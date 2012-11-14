package pl.psnc.dl.wf4ever.model.RDF;

import java.net.URI;

/**
 * The root class for the model.
 * 
 * @author piotrekhol
 * 
 */
public class Thing {

    /** resource URI. */
    protected URI uri;


    /**
     * Constructor.
     */
    public Thing() {
    }


    /**
     * Constructor.
     * 
     * @param uri
     *            resource URI
     */
    public Thing(URI uri) {
        this.uri = uri;
    }


    public URI getUri() {
        return uri;
    }


    public void setUri(URI uri) {
        this.uri = uri;
    }


    @Override
    public int hashCode() {
        return super.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Thing)) {
            return false;
        }
        Thing that = (Thing) obj;
        return that.uri.equals(this.uri);
    }


    @Override
    public String toString() {
        return getUri().toString();
    }

}
