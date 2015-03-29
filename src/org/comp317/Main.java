

import java.util.Random;

public class Main {
	static Random r = new Random(100);

	static String[] map = new String[]{"a","b"};

    public static void main(String[] args)
    {

	    String[] testerino = new String[0];


	    for(int i = 0; i < testerino.length; i++)
	    {
		    String sss = rando(200);
		    testerino[i] = sss;
		    //System.out.println(sss);
	    }

	    int runs = 1;

	    long startTime = System.nanoTime();
	    System.err.println("start");

	    Sorter s = new Sorter(30, 7, "",false);
	    s.sort(testerino, "output");

	    long endTime = System.nanoTime() - startTime;

	    System.err.println(endTime / 1000000);
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
