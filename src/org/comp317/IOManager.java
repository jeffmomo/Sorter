package org.comp317;

import sun.reflect.annotation.ExceptionProxy;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Jeff on 20/03/2015.
 */
public class IOManager
{
	public static int GZIPPED = 1;
	public static int NORMAL = 0;

	private int _type;

	public IOManager(int type)
	{
		_type = type;
	}

	public OutputStreamWriter createOutputStreamWriter(String fname) throws Exception
	{
		if(_type == 0)
			return new OutputStreamWriter((new FileOutputStream(fname)));
		else
			return new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(fname)));
	}

	public BufferedReader createBufferedReader(String fname) throws Exception
	{
		if(_type == 0)
			return new BufferedReader(new InputStreamReader((new FileInputStream(fname))));
		else
			return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fname))));
	}

	public int getType()
	{
		return _type;
	}

}
