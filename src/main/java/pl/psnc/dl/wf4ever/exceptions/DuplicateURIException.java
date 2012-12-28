package pl.psnc.dl.wf4ever.exceptions;

/**
 * Attempt to use URI that has already been used.
 */
public class DuplicateURIException extends Exception {

    /**
     * ID .
     */
    private static final long serialVersionUID = 1L;


    /**
     * Constructor.
     * 
     * @param message
     *            message
     */
    public DuplicateURIException(String message) {
        super(message);
    }


    /**
     * Constructor.
     * 
     * @param message
     *            message
     * @param e
     *            cause
     */
    public DuplicateURIException(String message, Exception e) {
        super(message, e);
    }
}
