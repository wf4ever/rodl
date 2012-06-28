package pl.psnc.dl.wf4ever.rosrs;

import java.net.URI;

import javax.ws.rs.core.Response;

public final class ROSRService {

    /**
     * Aggregate a new internal resource in the research object, upload the resource and create a proxy for it.
     * 
     * @param researchObject
     *            the research object
     * @param resource
     *            resource URI, must be internal
     * @param content
     *            resource content
     * @param contentType
     *            resource content type
     * @param original
     *            original resource in case of using a format specific URI
     * @return 201 Created pointing to the proxy URI
     */
    public static Response aggregateInternalResource(URI researchObject, URI resource, String content,
            String contentType, String original) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * Check if the research object defines a proxy with a given URI
     * 
     * @param researchObject
     *            research object to search
     * @param resource
     *            resource that may be a proxy
     * @return true if the resource is a proxy, false otherwise
     */
    public static boolean isProxy(URI researchObject, URI resource) {
        // TODO Auto-generated method stub
        return false;
    }


    /**
     * Checks if there exists a proxy that has ore:proxyFor pointing to the resource.
     * 
     * @param researchObject
     *            research object to search in
     * @param resource
     *            resource which should be pointed
     * @return true if a proxy was found, false otherwise
     */
    public static boolean existsProxyFor(URI researchObject, URI resource) {
        // TODO Auto-generated method stub
        return false;
    }


    /**
     * Return the ore:proxyFor object of a proxy.
     * 
     * @param researchObject
     *            research object in which the proxy is
     * @param proxy
     *            the proxy URI
     * @return the proxyFor object URI
     */
    public static URI getProxyFor(URI researchObject, URI proxy) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * Checks if there exists an annotation that has ao:body pointing to the resource.
     * 
     * @param researchObject
     *            research object to search in
     * @param resource
     *            resource which should be pointed
     * @return true if an annotation was found, false otherwise
     */
    public static boolean isAnnotationBody(URI researchObject, URI resource) {
        // TODO Auto-generated method stub
        return false;
    }


    /**
     * Add a proxy to the research object.
     * 
     * @param researchObject
     *            the research object
     * @param proxyFor
     *            resource for which the proxy is
     * @return 201 Created response pointing to the proxy
     */
    public static Response addProxy(URI researchObject, URI proxyFor) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * Check if the research object aggregates a resource.
     * 
     * @param researchObject
     *            the research object
     * @param resource
     *            the resource URI
     * @return true if the research object aggregates the resource, false otherwise
     */
    public static boolean isAggregatedResource(URI researchObject, URI resource) {
        // TODO Auto-generated method stub
        return false;
    }


    /**
     * Update an aggregated resource, which may be a regular file or RO metadata.
     * 
     * @param researchObject
     *            research object which aggregates the resource
     * @param resource
     *            resource URI
     * @param entity
     *            resource content
     * @param contentType
     *            resource content type
     * @param original
     *            original resource in case of using a format specific URI
     * @return 200 OK response
     */
    public static Response updateInternalResource(URI researchObject, URI resource, String entity, String contentType,
            String original) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * Return the content of an internal resource.
     * 
     * @param researchObject
     *            research object aggregating the resource
     * @param resource
     *            resource URI
     * @param accept
     *            requested MIME type
     * @param original
     *            original resource in case of using a format specific URI
     * @return 200 OK with resource content
     */
    public static Response getInternalResource(URI researchObject, URI resource, String accept, String original) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * Delete an internal resource, de-aggregate it and delete its proxy.
     * 
     * @param researchObject
     *            research object aggregating the resource
     * @param resource
     *            resource to delete
     * @param original
     *            original resource in case of using a format specific URI
     * @return 204 No Content response
     */
    public static Response deleteInternalResource(URI researchObject, URI resource, String original) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * An existing aggregated resource is being used as an annotation body now. Add it to the triplestore. If the
     * aggregated resource is external, do nothing.
     * 
     * @param researchObject
     *            research object aggregating the resource
     * @param resource
     *            URI of the resource that is converted
     */
    public static void convertAggregatedResourceToAnnotationBody(URI researchObject, URI resource) {
        // TODO Auto-generated method stub

    }


    /**
     * An aggregated resource is no longer an annotation body. Remove it from the triplestore. If the annotation body is
     * not inside the triplestore (i.e. it's an external resource), do nothing.
     * 
     * @param researchObject
     *            research object aggregating the resource
     * @param resource
     *            URI of the resource that is converted
     */
    public static void convertAnnotationBodyToAggregatedResource(URI researchObject, URI resource) {
        // TODO Auto-generated method stub

    }


    /**
     * Add a new annotation to the research object.
     * 
     * @param researchObject
     *            the research object
     * @param annotation
     *            annotation object, defining the target and body URIs
     * @return 201 Created response
     */
    public static Response addAnnotation(URI researchObject, Annotation annotation) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * Update the annotation targets and the annotation body of an annotation. This method does not add/delete the
     * annotation body to the triplestore.
     * 
     * @param researchObject
     *            the research object
     * @param annotation
     *            annotation object, defining the target and body URIs
     * @return 200 OK response
     */
    public static Response updateAnnotation(URI researchObject, Annotation annotation) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * Check if a resource is an annotation defined in a research object.
     * 
     * @param researchObject
     *            research object to search
     * @param resource
     *            resource that may be an annotation
     * @return true if this resource is an annotation in the research object, false otherwise
     */
    public static boolean isAnnotation(URI researchObject, URI resource) {
        // TODO Auto-generated method stub
        return false;
    }


    /**
     * Get the URI of an annotation body. If acceptHeader is not null, will return a format-specific URI.
     * 
     * @param researchObject
     *            research object in which the annotation is defined
     * @param annotation
     *            the annotation
     * @param acceptHeader
     *            MIME type requested (content negotiation) or null
     * @return URI of the annotation body
     */
    public static URI getAnnotationBody(URI researchObject, URI annotation, String acceptHeader) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * Delete an annotation. Does not delete the annotation body.
     * 
     * @param researchObject
     *            research object in which the annotation is defined
     * @param annotation
     *            the annotation
     * @return 204 No Content
     */
    public static Response deleteAnnotation(URI researchObject, URI annotation) {
        // TODO Auto-generated method stub
        return null;
    }

}
