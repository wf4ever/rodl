/**
 * 
 */
package pl.psnc.dl.wf4ever.sparql;

import java.io.InputStream;

import org.openrdf.rio.RDFFormat;

/**
 * @author piotrek
 * 
 */
public class QueryResult
{

	private final InputStream inputStream;

	private final RDFFormat format;


	/**
	 * @param inputStream
	 * @param format
	 */
	public QueryResult(InputStream inputStream, RDFFormat format)
	{
		this.inputStream = inputStream;
		this.format = format;
	}


	/**
	 * @return the inputStream
	 */
	public InputStream getInputStream()
	{
		return inputStream;
	}


	/**
	 * @return the format
	 */
	public RDFFormat getFormat()
	{
		return format;
	}

}
