package pl.psnc.dl.wf4ever.auth;

/**
 * @author nowakm
 */
@SuppressWarnings("serial")
public class ForbiddenException
	extends RuntimeException
{

	public ForbiddenException(String message)
	{
		super(message);
	}

}
