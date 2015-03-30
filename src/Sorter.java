

import java.io.*;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;


// Class to perform sorting
public class Sorter
{
	private boolean DEBUG = true;
	
	private StringHeap _heap;
	private int _runs;
	private int _maxFiles;
	private IOManager _IOMan;

	private BufferedWriter _currentStream;
	private BufferedWriter[] writers;
	private boolean[] writeIsClosed ;

	private int[] _fibSequence;
	private int[] _currentRuns;

	private int _totalRuns = 1;
	private int _totalPasses = 0;

	private String _outputFile;

	public Sorter(int bufferSize, int maxFiles, String tempDirectory, boolean gzip)
	{
		_heap = new StringHeap(bufferSize);
		_maxFiles = maxFiles;
                
		_IOMan = new IOManager((gzip? IOManager.GZIPPED: IOManager.NORMAL), "UTF-8", 8192, 8192, false, tempDirectory);
	}

	// Performs sorting
	public void sort(String[] data, String outputFile)
	{
		// Set initial parameters
		// Also calculate fibonacci sequence for approximate number of runs
		_outputFile = outputFile;
		_fibSequence = fibSequence(data.length / (_heap.capacity() * 2) + 1, _maxFiles);

		// Stores the amount of runs written per file. First file by default has to have 1 run
		_currentRuns = new int[_maxFiles - 1];
		_currentRuns[0] = 1;

		// Initialise run writer array
		writers = new BufferedWriter[_maxFiles];
		writeIsClosed = new boolean[_maxFiles];

		// Fill heap with data
		int i = 0, capacity = _heap.capacity();
		for(; i < capacity && i < data.length; i++)
		{
			_heap.insert(data[i]);
		}

		// Store the biggest string in run
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

				// Re-heap
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
				// If bigger, then put smallest of heap on run, and replace it with current item
				putStreamRuns(_heap.replace(data[i]));
			}
			else if(compare < 0)
			{
				// Otherwise just remove from heap and put on run
				// Send the current item to back of heap
				putStreamRuns(_heap.get());
				_heap.place(data[i]);
			}
			else
			{
				// If smallest in heap is same as current item, then just put on run without modifying heap
				putStreamRuns(data[i]);
			}
		}

		// Now all lines processed.
		// Need to flush out remaining content in heap

		// Re-heap
		_heap = new StringHeap(_heap.getBase());
		if(_heap.size() > 0)
		{
			// On to new run
			_runs = runFunction();

			// Add newlines if appending onto file
			if(writers[_runs] != null)
				putStreamRuns("");

			// Put the remaining content in heap out onto another run
			while (_heap.size() > 0)
			{
				putStreamRuns(_heap.get());
			}
		}
		log("Total Runs: " + _totalRuns);

		// Adding dummy runs
		int backup = _runs;

		// While dummy runs still could be added, add them
		while((_runs = runFunctionDummy()) != -1)
		{
			putStreamRuns("");
		}
		_runs = backup;

		log("Incl. Dummy: " + _totalRuns);

		// Flushes and closes all writers, as all runs have been created
		for(int g = 0; g < _maxFiles - 1; g++)
		{
			if(writers[g] != null)
			{
				try
				{
					writers[g].close();
					writeIsClosed[g] = true;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		// Perform merging
		merge();
	}

	// Deletes a file, given the file name
	private void deleteFile(String fname)
	{
		try
		{
			File f = _IOMan.getFile(fname);
			if(f.delete()){
				debugLog(f.getName() + " is deleted!");
			}else{
				debugLog("Delete operation is failed.");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// Returns the number of active readers
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
		// Initialise heap for merging
		KVHeap mergeHeap = new KVHeap(_maxFiles - 1);

		// Initialise file readers for runs
		BufferedReader[] fileReaders = new BufferedReader[_maxFiles];

		// Stores final run
		int finalRun = -1;

		// Stores whether its first pass or not
		boolean first = true;

		// Set initial file to write into
		_runs = _maxFiles - 1;

		// Process an arbitrary number of runs.
		// Until only 1 file left to do.
		// Unless its the first pass
		while(numReaders(fileReaders) > 1 || first)
		{
			first = false;

			// This file stores the next file to move into
			int tempruns = -1;

			// Go through all available files
			for (int i = 0; i < _maxFiles; i++)
			{
				// Incoming line
				String inLine = null;
				try
				{
					// Do not read from the file we're writing to
					if (i == _runs || !_IOMan.getFile("run" + i).exists())
						continue;

					// New file readers may need to be created for the current run being read
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

					_totalPasses++;
				}
				// If the empty string is read, then must have reached end of run. We just increment the read position by 1 to account for the newline symbol
				else if (!inLine.isEmpty())
				{
					// If a string is read, then increment the read position by the length of that string + newline
					// And then put the string (and its associated  onto the heap.

					mergeHeap.insert(new KVPair<Integer, String>(i, inLine));
				}
			}

			// Add a newline separator if there are already runs in current file stream
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

					_totalPasses++;
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
				finalRun = _runs;
				_runs = tempruns;
				try
				{
					// If tempruns has been modified, then one run must have been depleted.
					// Therefore we want to close the current writing stream
					_currentStream.close();
					_currentStream = null;
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			// End of processing an entire run
		}


		// End of processing all runs. Now only 1 single file left
		log("Total Passes: " + (_totalPasses - (_maxFiles - 2)));

		// Delete previous output
		if(_outputFile != null && new File(_outputFile).exists())
			deleteFile(_outputFile);


		// Do extra unzipping if output is gzipped
		if (_IOMan.getType() == IOManager.GZIPPED)
		{
			// Depending on nullity of output file, writes either to file or to system.out
			unzip("run" + (finalRun), _outputFile);
			deleteFile("run" + (finalRun));
		}
		else
		{
			// Output to stdout if no output file specified.
			if(_outputFile == null)
			{
				try
				{
					FileInputStream br = new FileInputStream(_IOMan.getDirectory() + "run" + (finalRun));

					byte[] buffer = new byte[1024];
					int length;
					while ((length = br.read(buffer)) > 0)
					{
						System.out.write(buffer, 0, length);
					}
				}
				catch (Exception e){e.printStackTrace();}

				System.out.flush();
			}
			else
			// If outputting to specified file, then just rename the final run file
			{
				// Perform renaming of final file
				File old = _IOMan.getFile("run" + finalRun);
				File output = _IOMan.getFile(_outputFile);

				if (old.renameTo(output))
					debugLog("FINISHED");
				else
					debugLog("Error in renaming final output");
			}

		}

	}

	// Performs unzipping of the a file
	private void unzip(String inName, String outName)
	{

		byte[] buffer = new byte[1024];

		try
		{
			GZIPInputStream gzStream = new GZIPInputStream(new FileInputStream(_IOMan.getDirectory() + inName));

			OutputStream out;
			if(outName != null)
				out = new FileOutputStream(_IOMan.getDirectory() + outName);
			else
				out = System.out;

			int len;
			while ((len = gzStream.read(buffer)) > 0)
			{
				out.write(buffer, 0, len);
			}

			gzStream.close();

			if(outName != null)
				out.close();

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	// Calculates which file the next run is going to go to
	private int runFunction()
	{
		_totalRuns++;
		int puttable = (_runs + 1) % (_maxFiles - 1);
		int firstOneChecked = puttable;

		// Evenly distributes the runs until it is no longer possible due to the fibonacci distribution

		while(_currentRuns[puttable] >= _fibSequence[puttable + 1])
		{
			puttable = (puttable + 1) % (_maxFiles - 1);

			// If, on some edge cases, the initial fibonacci sequence is too small for the number of runs (i.e. looped back to beginning again), then we must recalculate a new one
			if(puttable == firstOneChecked)
				_fibSequence = fibNext(_fibSequence);
		}

		_currentRuns[puttable] += 1;
		return puttable;
	}

	// Gets which file the next dummy run is going to go in to.
	// Returns -1 when the fibonacci sequence is fulfilled.
	private int runFunctionDummy()
	{
		_totalRuns++;
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

	// Puts the item into a file. Used in the initial run creation process
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
				debugLog(e.getMessage());
			}
		}
		try
		{
			_IOMan.write(writers[_runs], item + "\n");
		}catch (Exception e){e.printStackTrace();}
	}

	// Puts the item into a file. Used for the merging process
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
				debugLog(e.getMessage());
			}
		}
		try
		{
			_IOMan.write(_currentStream, item + "\n");
		}catch (Exception e){e.printStackTrace();}
	}

	// Gets the next 'layer' of fibonacci sequence, given the previous
	private int[] fibNext(int[] previousSequence)
	{
		//int total = previousSequence[previousSequence.length - 1];
		int output = -1;
		for(int i = 0;i<previousSequence.length;i++)
		{
			if(previousSequence[i] == 0)
				output = i;
		}
		if(output == -1)
			throw new Error("No zeros in sequence? Make sure its a fibonacci sequence");


		for(int i = 0;i<_maxFiles;i++)
		{
				previousSequence[i] += previousSequence[previousSequence.length - 1];
				//total += previousSequence[i];
		}

		previousSequence[previousSequence.length - 1] = 0;

		//if (total >= dataLength)
		{
			Arrays.sort(previousSequence);
			return (previousSequence);
		}

	}

	// Produces a fibonacci sequence, given the number of runs and the max number of files.
	private int[] fibSequence(int dataLength, int maxFiles)
	{
		int[] returnArray = new int[maxFiles];
		returnArray[maxFiles-1] = 1;
		int output = maxFiles-1;
		int total = 1;

		printRuns(returnArray,maxFiles,total);
		while (true)
		{
			for(int i = 0;i<maxFiles;i++)
			{
				if (i != output)
				{
					returnArray[i] += returnArray[output];
					total += returnArray[i];
				}
			}
			returnArray[output] = 0;
			printRuns(returnArray,maxFiles,total);
			if (total >= dataLength)
			{
				Arrays.sort(returnArray);
				return (returnArray);
			}
			total = 0;
			output--;

			if(output <0)
			{
				output += (maxFiles);
			}
		}
	}
	private void printRuns( int[] runsArray, int files,int total)
	{
		for(int i = 0; i < files; i++)
		{
			debugLog(runsArray[i] + " ");
		}
		debugLog( "total: " + total + " \n");
	}


	private void debugLog(String output, Object ... args)
	{
		if(DEBUG)
			System.err.println(String.format(output, args));
	}
	private void log(String output, Object ... args)
	{
		System.err.println(String.format(output, args));
	}


}
