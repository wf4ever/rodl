package pl.psnc.dl.wf4ever.exceptions;


public class ManifestTraversingException extends Exception {

    private static final long serialVersionUID = 1L;
    
    private static String message = "The manifest file cannot be properly read";
    
    public ManifestTraversingException() {
        super(message);
    }
    
    
    public ManifestTraversingException(String message) {
        super(message);
    }
    
    public ManifestTraversingException(String message, Exception e) {
        super(message, e);
    }
}
