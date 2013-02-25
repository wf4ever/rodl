package pl.psnc.dl.wf4ever.exceptions;

/**
 * <p>
 * A runtime exception thrown when user does not have permission to access particular resource.
 * </p>
 * 
 * @author nowakm
 */
@SuppressWarnings("serial")
public class ForbiddenException extends RuntimeException {

    /**
     * Constructor.
     * 
     * @param message
     *            message
     */
    public ForbiddenException(String message) {
        super(message);
    }

}
