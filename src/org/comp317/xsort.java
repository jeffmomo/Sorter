
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

public class xsort
{
    
    public static void main(String[] args)
    {
        int runSize = 7;
        int numFiles = 7;
        String  tempDir = "",
                outputFileName = "",
                inputFileName = "";
        boolean stdinInput = true;
        boolean gzip = false;

        // options
        if(args.length >0)
        {
            for(int i = 0; i <args.length;i++)
            {
                if(args[i].equals("-r"))
                {	
                    // setting run size (buffer size)(default 7)
                    runSize = Integer.parseInt(args[i+1]);
                    if(runSize <7)
                    {
                        System.err.println("error:: run size too small (must be at least 7)");
                        System.exit(1);
                    }
                    i++;
                } else if(args[i].equals("-k"))
                {	
                    // setting number of files (default 7)
                    numFiles = Integer.parseInt(args[i+1]);
                    i++;
                } else if(args[i].equals("-d"))
                {	
                    // setting temp dir 
                    tempDir = args[i+1] +"\\";
                    File f = new File(tempDir);
                    f.mkdirs();
                    i++;
                } else if(args[i].equals("-o"))
                {	
                    // setting output file name
                    outputFileName = args[i+1];
                    i++;
                }
                else if(args[i].equals("-gzo"))
                {	
                    // enable gzip
                    gzip = true;
                    outputFileName = args[i+1];
                    i++;
                } 
            }
        }
		
		// input
        String[] inputArray = null;
        // if input file name specified
        if(args.length%2 == 1)
        {
            stdinInput = false;
            inputFileName = args[args.length-1];
            try
            {
                // take input from file name
                BufferedReader br = new BufferedReader(new FileReader(inputFileName));                
                inputArray = readStreamTillEnd(br, stdinInput);
            }catch(IOException e){e.printStackTrace();}
        } else
        {
            // otherwise take from stdin stream
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            inputArray = readStreamTillEnd(br, stdinInput);
        }
        // printing information out
        System.err.println("total lines: " + inputArray.length);
        System.err.println("run size: " + runSize + "\nNum files: " + numFiles +"\ntempdir: " +tempDir + "\nin file: " + inputFileName + "\nout file: " + outputFileName + "\nstdinput: \n");

        // run sort merge
        Sorter s = new Sorter(runSize, numFiles, tempDir,gzip);
        if(!outputFileName.isEmpty())
            s.sort(inputArray, outputFileName);
        else
            s.sort(inputArray, null);
    }
	
    /**
	* reads from reader and parses data into a string array
	*/
    public static String[] readStreamTillEnd(BufferedReader br, boolean stdinStream)
    {
        LinkedList<String> linesList = new LinkedList<String>();
        if(stdinStream)
        {	
            // reading from stdin
            try 
            {
                String line = br.readLine();
                while(!line.equals(""))
                {
	                linesList.add(line);
                    line = br.readLine();
                }            
            } catch(IOException e){e.printStackTrace();}
        } else
        {	
            // reading from file
            try 
            {
                String line = br.readLine();
                while(line != null)
                {
                    linesList.add(line);
                    line = br.readLine();
                }            
            } catch(IOException e){e.printStackTrace();}
        }
        return linesList.toArray(new String[linesList.size()]);
    }
}