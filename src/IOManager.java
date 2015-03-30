
import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A Façade which handles most of the file-related operations.
 */
public class IOManager
{
	public static int GZIPPED = 1;
	public static int NORMAL = 0;



	private int _type;
	private String _charset;
	private int _writeBufferSize, _readBufferSize;
	private boolean _autoFlush;
	private String _tempDirectory;

	public IOManager(int type, String charset, int writeBufferSize, int readBufferSize, boolean autoFlush, String tempDirectory)
	{
		_type = type;
		_charset = charset;
		_writeBufferSize = writeBufferSize;
		_readBufferSize = readBufferSize;
		_autoFlush = autoFlush;
		_tempDirectory = tempDirectory.isEmpty() ? "" : tempDirectory + File.separator;
	}

	// Creates a writer given a file name
	public BufferedWriter createOutputStreamWriter(String fname) throws Exception
	{
		if(_type == 0)
			return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_tempDirectory + fname), _charset), _writeBufferSize);
		else
			return new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(_tempDirectory + fname), _writeBufferSize, false), _charset), _writeBufferSize);
	}

	// Creates a reader given a file name
	public BufferedReader createBufferedReader(String fname) throws Exception
	{
                if(_type == 0)
	                return new BufferedReader(new InputStreamReader(new FileInputStream(_tempDirectory + fname), _charset), _readBufferSize);
		else
	                return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(_tempDirectory + fname), _readBufferSize), _charset), _readBufferSize);
	}

	// Writes to a given write the string
	public void write(BufferedWriter writer, String item) throws IOException
	{
		writer.write(item);
		if(_autoFlush)
			writer.flush();
	}

	// Creates a file handle
	public File getFile(String fname)
	{
		return new File(_tempDirectory + fname);
	}

	// Gets the type of the IOManager, i.e. either Gzipped or Normal
	public int getType()
	{
		return _type;
	}

	// Gets the temporary directory specified in the constructor
	public String getDirectory()
	{
		return _tempDirectory;
	}

}
