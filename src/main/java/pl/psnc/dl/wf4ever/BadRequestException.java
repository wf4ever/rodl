package pl.psnc.dl.wf4ever;


/**
 * This exception indicates that the request is incorrect.
 * 
 * @author piotrekhol
 * 
 */
public class BadRequestException extends Exception {

    /** id. */
    private static final long serialVersionUID = -3266796330906295320L;


    /**
     * Constructor.
     * 
     * @param message
     *            the message
     */
    public BadRequestException(String message) {
        super(message);
    }


    public BadRequestException(String message, Exception e) {
        super(message, e);
    }

}
