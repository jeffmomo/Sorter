package org.comp317;

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
	private String _charset;
	private int _bufferSize;

	public IOManager(int type, String charset, int bufferSize)
	{
		_type = type;
		_charset = charset;
		_bufferSize = bufferSize;
	}

	public BufferedWriter createOutputStreamWriter(String fname) throws Exception
	{
		if(_type == 0)
			return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fname), _charset), _bufferSize);
		else
			return new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(fname)), _charset), _bufferSize);
	}

	public BufferedReader createBufferedReader(String fname) throws Exception
	{
		if(_type == 0)
			return new BufferedReader(new InputStreamReader(new FileInputStream(fname), _charset));
		else
			return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fname)), _charset));
	}

	public int getType()
	{
		return _type;
	}

}
