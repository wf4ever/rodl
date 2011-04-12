package pl.psnc.dl.wf4ever.auth;

/**
 * <p>A runtime exception thrown when user does not have permission to access particular resource.</p>
 *
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
