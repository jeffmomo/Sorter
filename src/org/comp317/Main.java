package org.comp317;

import java.util.Random;

public class Main {
	static Random r = new Random(100);

	static String[] map = new String[]{"a","b","c","d","e","f","g"};

    public static void main(String[] args)
    {

	    String[] testerino = new String[100];

	    for(int i = 0; i < 100; i++)
	    {
		    String sss = rando(10);
		    testerino[i] = sss;
		    //System.out.println(sss);
	    }

	    int runs = 1;
	    int memForRun = testerino.length / (runs * 2);


	    Sorter s = new Sorter(3, 7);
	    s.sort(testerino);
    }

	private static String rando(int len)
	{
		String out = "";
		for(int i = 0; i < len; i++)
		{
			out += map[((int)(r.nextDouble() * 1000)) % map.length];
		}
		return out;
	}

}
