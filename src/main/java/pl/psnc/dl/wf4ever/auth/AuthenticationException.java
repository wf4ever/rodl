package pl.psnc.dl.wf4ever.auth;

/**
 * <p>
 * A runtime exception representing a failure to provide correct authentication credentials.
 * </p>
 */
@SuppressWarnings("serial")
public class AuthenticationException extends RuntimeException {

    /**
     * Constructor.
     * 
     * @param message
     *            message
     * @param realm
     *            realm
     */
    public AuthenticationException(String message, String realm) {
        super(message);
        this.realm = realm;
    }


    /** authentication realm. */
    private String realm = null;


    public String getRealm() {
        return this.realm;
    }

}
