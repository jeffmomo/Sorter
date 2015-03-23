package org.comp317;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class xsort
{
    
    public static void main(String[] args)
    {
        int runSize = 7;
        int numFiles = 7;
        String  tempDir = "",
                outputFileName = "",
                inputFileName = "", 
                totalInput = "";
        boolean stdinInput = true,
                stdOutput = true;
        
        if(args.length >0)
        {
            for(int i = 0; i <args.length;i++)
            {
                if(args[i].equals("-r"))
                {
                    
                    runSize = Integer.parseInt(args[i+1]);
                    if(runSize <7)
                    {
                        System.err.println("error:: run size too small (must be at least 7)");
                        System.exit(1);
                    }
                    i++;
                } else if(args[i].equals("-k"))
                {
                    numFiles = Integer.parseInt(args[i+1]);
                    i++;
                } else if(args[i].equals("-d"))
                {
                    tempDir = args[i+1];
                    i++;
                } else if(args[i].equals("-o"))
                {
                    outputFileName = args[i+1];
                    stdOutput = false;
                    i++;
                }
            }
            
            // input file specified
            if(args.length%2 == 1)
            {
                stdinInput = false;
                inputFileName = args[args.length-1];
            }
        }
        
        if (stdinInput)
        { 
            
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input;            
            while (!(input = readLine(br)).equals(""))
            {
                //System.out.println(input);
                totalInput += input + "\n";
            }
        }
        String[] inputArray = totalInput.split("\n");
        
        Sorter s = new Sorter(runSize, numFiles);        
        s.sort(inputArray);
        System.out.println("total lines: " + inputArray.length);
        System.out.println("run size: " + runSize + "\nNum files: " + numFiles +"\ntempdir: " +tempDir + "\nin file: " + inputFileName + "\nout file: " + outputFileName + "\nstdinput: \n" + totalInput);
        
    }
    
    
    public static String readLine(BufferedReader br)
    {
        String line = null;
        try {
           line = br.readLine();
        } catch (IOException ioe) {
           System.err.println("error reading");
           System.exit(1);
        }
        return line;
    }

	
}