package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;

import org.openrdf.rio.RDFFormat;

/**
 * A resource that has its serializable representation - aggregated resources and resource maps.
 * 
 * @author piotrekhol
 * 
 */
public interface ResearchObjectComponent {

    /**
     * Return an RDF format specific URI.
     * 
     * @param format
     *            RDF format
     * @return the URI
     */
    URI getUri(RDFFormat format);


    /**
     * Get the resource filename, i.e. the last segment of the path, decoded.
     * 
     * @return the filename
     */
    String getName();


    /**
     * Get the research object in which the component is stored.
     * 
     * @return a research object
     */
    ResearchObject getResearchObject();


    /**
     * Check if the resource is internal. Resource is internal only if its content has been deployed under the control
     * of the service. A resource that has "internal" URI but the content has not been uploaded is considered external.
     * 
     * @return true if the resource content is deployed under the control of the service, false otherwise
     */
    boolean isInternal();

}
