package pl.psnc.dl.wf4ever.exceptions;

/**
 * The Jena model that is processed is in some way wrong according to the RO ontology.
 * 
 * @author piotrekhol
 * 
 */
public class IncorrectModelException extends Exception {

    /** id. */
    private static final long serialVersionUID = -8946511066283873662L;


    /**
     * Constructor.
     * 
     * @param message
     *            message
     */
    public IncorrectModelException(String message) {
        super(message);
    }


    /**
     * Constructor.
     * 
     * @param message
     *            message
     * @param e
     *            reason
     */
    public IncorrectModelException(String message, Exception e) {
        super(message, e);
    }
}
