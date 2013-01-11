package pl.psnc.dl.wf4ever.model.ORE;

import java.net.URI;
import java.util.Map;

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


    /**
     * Get the aggregated resources.
     * 
     * @return a map of aggregated resources by their URI
     */
    Map<URI, AggregatedResource> getAggregatedResources();


    /**
     * Get a map of proxies.
     * 
     * @return proxies for aggregated resource, mapped by proxy URIs.
     */
    Map<URI, ? extends Proxy> getProxies();
}
