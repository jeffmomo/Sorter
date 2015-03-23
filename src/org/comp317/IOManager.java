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
	private int _writeBufferSize, _readBufferSize;
	private boolean _autoFlush;

	public IOManager(int type, String charset, int writeBufferSize, int readBufferSize, boolean autoFlush)
	{
		_type = type;
		_charset = charset;
		_writeBufferSize = writeBufferSize;
		_readBufferSize = readBufferSize;
		_autoFlush = autoFlush;
	}

	public BufferedWriter createOutputStreamWriter(String fname) throws Exception
	{

		if(_type == 0)
			return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fname), _charset), _writeBufferSize);
		else
			return new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(fname), _writeBufferSize, false), _charset), _writeBufferSize);
	}

	public BufferedReader createBufferedReader(String fname) throws Exception
	{

		if(_type == 0)
			return new BufferedReader(new InputStreamReader(new FileInputStream(fname), _charset), _readBufferSize);
		else
			return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fname), _readBufferSize), _charset), _readBufferSize);
	}

	public void write(BufferedWriter writer, String item) throws IOException
	{

		writer.write(item);
		if(_autoFlush)
			writer.flush();
	}

	public int getType()
	{
		return _type;
	}

}
