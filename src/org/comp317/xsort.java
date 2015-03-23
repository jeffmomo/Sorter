package org.comp317;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;

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
        }
        String[] inputArray = null;
        // input file specified
        if(args.length%2 == 1)
        {
            stdinInput = false;
            inputFileName = args[args.length-1];
            try
            {
                BufferedReader br = new BufferedReader(new FileReader(inputFileName));                
                inputArray = readStreamTillEnd(br, stdinInput);
            }catch(IOException e){e.printStackTrace();}
        } else
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            inputArray = readStreamTillEnd(br, stdinInput);
        }
        System.out.println("total lines: " + inputArray.length);
        System.out.println("run size: " + runSize + "\nNum files: " + numFiles +"\ntempdir: " +tempDir + "\nin file: " + inputFileName + "\nout file: " + outputFileName + "\nstdinput: \n");
        System.out.println("derp");  
        for (int i = 0; i< inputArray.length;i++)
        {
             System.out.println(inputArray[i]);            
        }
        System.out.println("derp");
        Sorter s = new Sorter(runSize, numFiles);
        s.sort(inputArray);


        
        
        
    }
    
    public static String[] readStreamTillEnd(BufferedReader br, boolean stdinStream)
    {
        LinkedList<String> linesList = new LinkedList<String>();
        if(stdinStream)
        {
            try 
            {
                String line = br.readLine();
                while(!line.equals(""))
                {

                    line = br.readLine();
                    linesList.add(line);
                }            
            } catch(IOException e){e.printStackTrace();}
        } else
        {
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