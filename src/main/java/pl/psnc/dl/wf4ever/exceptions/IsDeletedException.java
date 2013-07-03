package pl.psnc.dl.wf4ever.exceptions;

/**
 * This exception indicates that the resource (RO) has already been deleted.
 * 
 * @author piotrekhol
 * 
 */
public class IsDeletedException extends Exception {

    /** id. */
    private static final long serialVersionUID = -3266796330906295320L;


    /**
     * Constructor.
     * 
     * @param message
     *            the message
     */
    public IsDeletedException(String message) {
        super(message);
    }


    /**
     * Constructor.
     * 
     * @param message
     *            the message
     * @param e
     *            the original exception
     */
    public IsDeletedException(String message, Exception e) {
        super(message, e);
    }

}
