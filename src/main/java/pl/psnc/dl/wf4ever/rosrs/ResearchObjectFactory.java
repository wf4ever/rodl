package pl.psnc.dl.wf4ever.rosrs;

import java.net.URI;

import pl.psnc.dl.wf4ever.common.ResearchObject;

/**
 * A class for managing Research Object DAOs.
 * 
 * @author piotrekhol
 * 
 */
public final class ResearchObjectFactory {

    /**
     * Private constructor.
     */
    private ResearchObjectFactory() {
        //nope
    }


    /**
     * Load a research object or create a new instance.
     * 
     * @param uri
     *            RO URI
     * @return a research object with its URI set and other details loaded, if possible
     */
    public static ResearchObject get(URI uri) {
        return new ResearchObject(uri);
    }
}
