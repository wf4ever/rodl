package pl.psnc.dl.wf4ever.exceptions;

public class ManifestTraversingException extends Exception {

    private static final long serialVersionUID = 1L;


    public ManifestTraversingException(String message) {
        super(message);
    }


    public ManifestTraversingException(String message, Exception e) {
        super(message, e);
    }
}
