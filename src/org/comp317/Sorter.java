package org.comp317;

import java.io.*;


/**
 * Created by Jeff on 16/03/2015.
 */
public class Sorter
{
	private StringHeap _heap;
	private int _runs;
	private int _maxFiles;
	private OutputStreamWriter _currentStream;
	private IOManager _IOMan = new IOManager(IOManager.NORMAL);

	private OutputStreamWriter[] writers;
	private boolean[] writeIsClosed ;

	private long[] _readPosition;


	private int zz = 0;

	public Sorter(int bufferSize, int maxFiles)
	{
		_heap = new StringHeap(bufferSize);
		_maxFiles = maxFiles;
	}

	public void sort(String[] data)
	{
		writers = new OutputStreamWriter[_maxFiles];
		writeIsClosed = new boolean[_maxFiles];

		// Fill heap with data
		int i = 0, capacity = _heap.capacity();
		for(; i < capacity; i++)
		{
			_heap.insert(data[i]);
		}

		String _biggestInRun;
		// Loops through data
		for(; i < data.length; i++)
		{
			if(_heap.size() == 0)
			{
				// End of 1 run

				// Increment runs according to function
				_runs = runFunction();
				if(writers[_runs] != null)
					putStreamRuns("");

				// Reheap
				_heap = new StringHeap(_heap.getBase());
			}

			// Gets smallest item in heap
			_biggestInRun = _heap.peek();
			if(_biggestInRun == null)
				break;

			// Compares the incoming item with the smallest item in heap
			int compare = data[i].compareTo(_biggestInRun);
			if(compare > 0)
			{
				putStreamRuns(_heap.replace(data[i]));
			}
			else if(compare < 0)
			{
				putStreamRuns(_heap.get());
				_heap.place(data[i]);
			}
			else
			{
				putStreamRuns(data[i]);
			}
		}

		// After all lines processed flush out content in heap
		_heap = new StringHeap(_heap.getBase());
		while(_heap.size() > 0)
		{
			putStreamRuns(_heap.get());
		}

		// Flushes and closes all writers, as all runs have been created
		for(int g = 0; g < _maxFiles - 1; g++)
		{
			try
			{
				writers[g].flush();
				writers[g].close();
				writeIsClosed[g] = true;
			}catch (Exception e){e.printStackTrace();}
		}

		merge();
	}

	// Deletes a file, given the file name
	private void deleteFile(String fname)
	{
		try
		{
			File f = new File(fname);
			if(f.delete()){
				System.out.println(f.getName() + " is deleted!");
			}else{
				System.out.println("Delete operation is failed.");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	// Merges existing runs
	private void merge()
	{
		KVHeap mergeHeap = new KVHeap(_maxFiles);
		_readPosition = new long[_maxFiles];
		BufferedReader[] fileReaders = new BufferedReader[_maxFiles];

		// Set initial file to write into
		_runs = _maxFiles - 1;

		// Process an arbitrary number of runs.
		// Use higher values if neccesary to complete the merge
		// The number needs to be calculated thru fibbonacci or something
		for(int z = 0; z < 50; z++)
		{
			int tempruns = -1;
			for (int i = 0; i < _maxFiles; i++)
			{
				// Incoming line
				String inLine = null;
				try
				{
					// Do not read from the file we're writing to
					if (!new File("run" + i).exists() || i == _runs)
						continue;
					if (fileReaders[i] == null)
						fileReaders[i] = _IOMan.createBufferedReader("run" + i);

					inLine = fileReaders[i].readLine();
				} catch (Exception e)
				{
					e.printStackTrace();
				}

				// If the incoming line is null, then must have reached end of that file.
				// So we delete that file and reset its record of reading, such that next time we read this file we read from the beginning
				if (inLine == null)
				{
					_readPosition[i] = 0;
					tempruns = i;
					try
					{
						fileReaders[i].close();
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					;
					deleteFile("run" + i);
					fileReaders[i] = null;
				}
				// If the empty string is read, then must have reached end of run. We just increment the read position by 1 to account for the newline symbol
				else if (inLine.equals(""))
				{
					_readPosition[i] += 1;
				} else
				{
					// If a string is read, then increment the read position by the length of that string + newline
					// And then put the string (and its associated  onto the heap.
					_readPosition[i] += inLine.getBytes().length + 1;
					mergeHeap.insert(new KVPair<Integer, String>(i, inLine));
				}
			}

			if(_currentStream != null)
				putStreamMerge("");


			// While there are items in the merging heap
			// Do a n-way merge
			while (mergeHeap.size() > 0)
			{
				// Get smallest item in heap
				KVPair<Integer, String> out = mergeHeap.peek();
				int key = out.key;

				String inLine = null;
				try
				{
					// If the file where the smallest item in heap came from is not depleted
					// Then we read from it
					if (fileReaders[key] != null)
						inLine = fileReaders[key].readLine();
				} catch (Exception e)
				{
					e.printStackTrace();
				}

				// If inLine is null then that file is depleted
				if (inLine == null)
				{
					_readPosition[key] = 0;

					// Remove the smallest item in that heap
					mergeHeap.get();

					// Set the depleted file to be the file we write stuff into next
					tempruns = key;
					try
					{
						// Close the reader on depleted file
						fileReaders[key].close();

					} catch (Exception e)
					{
						e.printStackTrace();
					}

					// Delete the file and set references to null
					deleteFile("run" + key);
					fileReaders[key] = null;
				}
				else // If not null
				{

					_readPosition[key] += inLine.getBytes().length + 1;

					// If end of a run in this file, then we just remove top from heap
					if (inLine.equals(""))
						mergeHeap.get();
					else
					// Otherwise we replace top with the newly read line
						mergeHeap.replace(new KVPair<Integer, String>(key, inLine));
				}

				// Put the top of heap into the file currently being written
				putStreamMerge(out.value);
			}

			// If a file has been read to depletion the tempruns variable would not be -1
			if (tempruns != -1)
			{
				// Set the new writing destination
				_runs = tempruns;
				try
				{
					// If tempruns has been modified, then one run must have been depleted.
					// Therefore we want to close the current writing stream
					_currentStream.flush();
					_currentStream.close();
					_currentStream = null;
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			// End of processing an entire run
			System.out.println("pass");
		}
	}

	private int runFunction()
	{
//		double total = 0;
//		for(int i = 0; i < _maxFiles - 1; i++)
//		{
//			total += 1 * Math.pow(1.618, i);
//		}
//
//		double rand = Math.random();
//
////		for(int i = 0; i < _maxFiles - 2; i++)
////		{
////			if(rand < (i / total))
////				return i;
////		}
//
//		double prob = ((Math.pow(1.618, _runs + 1)) / total);
//		if(rand < prob)
//		{
//			return (_runs + 1) % (_maxFiles - 1);
//		}
//		else return _runs;
//
//
//		//return _maxFiles - 1;
		return (_runs + 1) % (_maxFiles - 1);
	}

	private void putStreamRuns(String item)
	{
		if(writers[_runs] == null || writeIsClosed[_runs])
		{
			writeIsClosed[_runs] = false;
			try
			{
				writers[_runs] = _IOMan.createOutputStreamWriter("run" + _runs);
			}
			catch(Exception e)
			{
				System.err.println(e.getMessage());
			}
		}
		try
		{
			writers[_runs].write(item + "\n");
		}catch (Exception e){e.printStackTrace();}
		zz++;
	}

	private void putStreamMerge(String item)
	{
		if(_currentStream == null)
		{
			try
			{
				_currentStream = _IOMan.createOutputStreamWriter("run" + _runs);
			}
			catch(Exception e)
			{
				System.err.println(e.getMessage());
			}
		}
		try
		{
			_currentStream.write(item + "\n");
		}catch (Exception e){e.printStackTrace();}

		zz++;
	}


	private void getRun()
	{

	}


}
