package pl.psnc.dl.wf4ever.model.ORE;

import java.net.URI;

/**
 * ore:Aggregation, for example ro:ResearchObject and ro:Folder.
 * 
 * @author piotrekhol
 * 
 */
public interface Aggregation {

    /**
     * Get the aggregation URI.
     * 
     * @return the URI
     */
    URI getUri();


    /**
     * Get resource map describing this aggregation.
     * 
     * @return the resource map
     */
    ResourceMap getResourceMap();
}
