package pl.psnc.dl.wf4ever.model.ORE;

import java.net.URI;

/**
 * ore:Aggregation, for example ro:ResearchObject and ro:Folder.
 * 
 * @author piotrekhol
 * 
 */
public interface Aggregation {

    URI getUri();


    /**
     * Get resource map describing this aggregation.
     * 
     * @return
     */
    ResourceMap getResourceMap();

}
