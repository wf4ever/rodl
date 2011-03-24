package pl.psnc.dl.wf4ever.connection;

import java.io.IOException;
import java.io.InputStream;


/**
 * Hotfix wrapper for input stream provided by dLibra server, which
 * is does not comply API specification ({@link #read(byte[])} returns
 * 0 instead of -1 on EOF) and therefore cannot be directly passed
 * to Jersey.
 */
//TODO fix dLibra server and remove this class
public class EOFInputStream
	extends InputStream
{

	private InputStream input;

	public EOFInputStream(InputStream input)
	{
		this.input = input;
	}


	@Override
	public int read()
		throws IOException
	{
		return input.read();
	}


	@Override
	public int read(byte[] b)
		throws IOException
	{
		int read = input.read(b);
		if (read == 0 && b.length > 0) {
			return -1;
		}
		return read;
	}


	@Override
	public int read(byte[] b, int off, int len)
		throws IOException
	{
		int read = input.read(b, off, len);
		if (read == 0 && len > 0) {
			return -1;
		}
		return read;
	}
}
