package pl.psnc.dl.wf4ever.evo;

/**
 * Exception thrown by an operation when it fails gracefully.
 * 
 * @author piotrekhol
 * 
 */
public class OperationFailedException extends Exception {

    /** id. */
    private static final long serialVersionUID = -7402155750808674281L;


    public OperationFailedException(String message) {
        super(message);
    }

}
