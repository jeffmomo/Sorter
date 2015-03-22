package org.comp317;


import java.io.*;
import java.util.zip.GZIPInputStream;


/**
 * Created by Jeff on 16/03/2015.
 */
public class Sorter
{
	private StringHeap _heap;
	private int _runs;
	private int _maxFiles;
	private BufferedWriter _currentStream;
	private IOManager _IOMan = new IOManager(IOManager.NORMAL, "UTF-8", 65536, 65536);

	private BufferedWriter[] writers;
	private boolean[] writeIsClosed ;


	private int _fibParam = 0;
	private int[] _fibSequence;
	private int[] _currentRuns;

	private int zz = 0;

	public Sorter(int bufferSize, int maxFiles)
	{
		_heap = new StringHeap(bufferSize);
		_maxFiles = maxFiles;

	}

	public void sort(String[] data)
	{
		_fibParam = data.length / (_heap.capacity() * 2) + 1;
		_fibSequence = rund.fibSequence(_fibParam, _maxFiles);
		_currentRuns = new int[_maxFiles - 1];
		_currentRuns[0] = 1;

		writers = new BufferedWriter[_maxFiles];
		writeIsClosed = new boolean[_maxFiles];

		// Fill heap with data
		int i = 0, capacity = _heap.capacity();
		for(; i < capacity; i++)
		{
			_heap.insert(data[i]);
		}

		String biggestInRun;
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
			biggestInRun = _heap.peek();
			if(biggestInRun == null)
				break;

			// Compares the incoming item with the smallest item in heap
			int compare = data[i].compareTo(biggestInRun);
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
		if(_heap.size() > 0)
		{
			_runs = runFunction();
			if(writers[_runs] != null)
				putStreamRuns("");
			while (_heap.size() > 0)
			{
				putStreamRuns(_heap.get());
			}
		}

		// Adding dummy runs
		int backup = _runs;
		while((_runs = runFunctionDummy()) != -1)
		{
			putStreamRuns("");
		}
		_runs = backup;

		// Flushes and closes all writers, as all runs have been created
		for(int g = 0; g < _maxFiles - 1; g++)
		{
			try
			{
				//writers[g].flush();
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
				System.err.println(f.getName() + " is deleted!");
			}else{
				System.err.println("Delete operation is failed.");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private int numReaders(BufferedReader[] fr)
	{
		int out = 0;
		for(int i = 0; i < fr.length; i++)
		{
			if(fr[i] != null)
				out++;
		}

		return out;
	}


	// Merges existing runs
	private void merge()
	{
		KVHeap mergeHeap = new KVHeap(_maxFiles - 1);

		BufferedReader[] fileReaders = new BufferedReader[_maxFiles];
		int prevRun = -1;
		boolean first = true;

		// Set initial file to write into
		_runs = _maxFiles - 1;

		// Process an arbitrary number of runs.
		// Use higher values if neccesary to complete the merge
		// The number needs to be calculated thru fibbonacci or something
		//for(int z = 0; z < 1000; z++)
		while(numReaders(fileReaders) > 1 || first)
		{
			first = false;
			int tempruns = -1;
			for (int i = 0; i < _maxFiles; i++)
			{
				// Incoming line
				String inLine = null;
				try
				{
					// Do not read from the file we're writing to
					if (i == _runs || !new File("run" + i).exists())
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

					tempruns = i;
					try
					{
						fileReaders[i].close();
					} catch (Exception e)
					{
						e.printStackTrace();
					}

					deleteFile("run" + i);
					fileReaders[i] = null;
				}
				// If the empty string is read, then must have reached end of run. We just increment the read position by 1 to account for the newline symbol
				else if (!inLine.isEmpty())
				{
					// If a string is read, then increment the read position by the length of that string + newline
					// And then put the string (and its associated  onto the heap.

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
					//if (fileReaders[key] != null)
					inLine = fileReaders[key].readLine();
				} catch (Exception e)
				{
					e.printStackTrace();
				}

				// If inLine is null then that file is depleted
				if (inLine == null)
				{


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
					// If end of a run in this file, then we just remove top from heap
					if (inLine.isEmpty())
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
				prevRun = _runs;
				_runs = tempruns;
				try
				{
					// If tempruns has been modified, then one run must have been depleted.
					// Therefore we want to close the current writing stream
					//_currentStream.flush();
					_currentStream.close();
					_currentStream = null;
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			// End of processing an entire run
			//System.err.println("pass");
		}


		if(new File("output").exists())
			deleteFile("output");

		if(_IOMan.getType() == IOManager.GZIPPED)
		{
			unzip("run" + (prevRun), "output");
			//deleteFile("run" + (prevRun));
		}
		else
		{
			File old = new File("run" + prevRun);
			File output = new File("output");

			if(old.renameTo(output)) 
			{
				System.err.println("FINISHED");
			} else {
				System.err.println("Error in renaming final output");
			}
		}
	}

	private void unzip(String inName, String outName)
	{

		byte[] buffer = new byte[1024];

		try
		{

			GZIPInputStream gzStream =
					new GZIPInputStream(new FileInputStream(inName));

			FileOutputStream out =
					new FileOutputStream(outName);

			int len;
			while ((len = gzStream.read(buffer)) > 0)
			{
				out.write(buffer, 0, len);
			}

			gzStream.close();
			out.close();

		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private int runFunction()
	{
		int puttable = (_runs + 1) % (_maxFiles - 1);
		int firstOneChecked = puttable;

		while(_currentRuns[puttable] >= _fibSequence[puttable + 1])
		{
			puttable = (puttable + 1) % (_maxFiles - 1);
			if(puttable == firstOneChecked)
				_fibSequence = rund.fibSequence(++_fibParam, _maxFiles);
		}

		_currentRuns[puttable] += 1;
		return puttable;
	}
	private int runFunctionDummy()
	{
		int puttable = (_runs + 1) % (_maxFiles - 1);
		int firstOneChecked = puttable;

		while(_currentRuns[puttable] >= _fibSequence[puttable + 1])
		{
			puttable = (puttable + 1) % (_maxFiles - 1);
			if(puttable == firstOneChecked)
				return -1;
		}

		_currentRuns[puttable] += 1;
		return puttable;
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
			//writers[_runs].flush();
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
			//_currentStream.flush();
		}catch (Exception e){e.printStackTrace();}

		zz++;
	}


}
