package pl.psnc.dl.wf4ever.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import pl.psnc.dl.wf4ever.vocabulary.AO;
import pl.psnc.dl.wf4ever.vocabulary.ORE;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * Tests for adding annotations and annotation bodies.
 * 
 * @author piotrekhol
 * 
 */
public class AnnotationTest extends ResourceBase {

    /** An annotation body path. */
    private final String annotationBodyPath = ".ro/ann1.ttl";

    /** The annotation body path in a different RDF format. */
    private final String annotationBodyURIRDF = ".ro/ann1.rdf";


    @Override
    public void setUp()
            throws Exception {
        super.setUp();
    }


    @Override
    public void tearDown()
            throws Exception {
        super.tearDown();
    }


    /**
     * Test you can add the annotation body.
     */
    @Test
    public void addAnnotationBody() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationBody.ttl");
        ClientResponse response = addAnnotation(is, ro, annotationBodyPath, accessToken);
        IOUtils.closeQuietly(is);
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
        model.read(response.getEntityInputStream(), null);
        model.write(System.out);
        Individual proxy = model.listIndividuals(ORE.Proxy).next();
        Individual resource = model.getIndividual(ro.resolve(annotationBodyPath).toString());
        Assert.assertNotNull(proxy);
        Assert.assertTrue(resource.hasRDFType(ORE.AggregatedResource));
        Assert.assertTrue(model.contains(proxy, ORE.proxyFor, resource));
        Assert.assertTrue(model.contains(resource, DCTerms.created, (RDFNode) null));
        Assert.assertTrue(model.contains(resource, DCTerms.creator, (RDFNode) null));
        Individual annotation = model.getIndividual(response.getLocation().toString());
        Assert.assertTrue(annotation.hasRDFType(RO.AggregatedAnnotation));
        Assert.assertTrue(model.contains(annotation, AO.body, resource));
        Assert.assertTrue(model.contains(annotation, RO.annotatesAggregatedResource, (RDFNode) null));
        Assert.assertTrue(model.contains(annotation, DCTerms.created, (RDFNode) null));
        Assert.assertTrue(model.contains(annotation, DCTerms.creator, (RDFNode) null));
        response.close();
        webResource.uri(ro).path(annotationBodyPath).header("Authorization", "Bearer " + accessToken)
                .delete(ClientResponse.class);
    }


    /**
     * Test you can retrieve the annotation body.
     */
    @Test
    public void getAnnotationBody() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationBody.ttl");
        ClientResponse response = addAnnotation(is, ro, annotationBodyPath, accessToken);
        IOUtils.closeQuietly(is);
        assertEquals(HttpStatus.SC_CREATED, response.getStatus());
        response.close();
        String body = webResource.uri(ro).path(annotationBodyPath).header("Authorization", "Bearer " + accessToken)
                .get(String.class);
        assertTrue("Annotation body should contain file path: a%20workflow.t2flow",
            body.contains("a%20workflow.t2flow"));
        assertTrue(body.contains("A test"));
    }


    /**
     * Test that it's possible to get an annotation body in a different format.
     * 
     * @throws IOException
     *             problem with test or response input streams
     * @throws ClientHandlerException
     *             jersey client problem
     * @throws UniformInterfaceException
     *             unexpected response status
     */
    @Test
    public void getAnnotationBodyWithContentNegotiation()
            throws UniformInterfaceException, ClientHandlerException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationBody.ttl");
        addAnnotation(is, ro, annotationBodyPath, accessToken).close();
        IOUtils.closeQuietly(is);
        ClientResponse response = webResource.uri(ro).path(annotationBodyURIRDF).queryParam("original", "ann1.ttl")
                .header("Authorization", "Bearer " + accessToken).get(ClientResponse.class);
        String body = IOUtils.toString(response.getEntityInputStream());
        assertEquals("application/rdf+xml", response.getType().toString());
        response.close();
        assertTrue("Annotation body should contain file path: a%20workflow.t2flow",
            body.contains("a%20workflow.t2flow"));
        assertTrue(body.contains("A test"));

    }


    /**
     * Test that it's possible to delete the annotation body.
     * 
     * @throws IOException
     *             could not close the test input stream
     */
    @Test
    public void deleteAnnotationBody()
            throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationBody.ttl");
        addAnnotation(is, ro, annotationBodyPath, accessToken);
        ClientResponse response = webResource.uri(ro).path(annotationBodyPath)
                .header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
        is.close();
        assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
        response.close();
    }


    /**
     * Test that it's possible to delete the annotation body using a reference to a different format.
     * 
     * @throws IOException
     *             could not close the test input stream
     */
    @Test
    public void deleteAnnotationBodyWithContentNegotiation()
            throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationBody.ttl");
        addAnnotation(is, ro, annotationBodyPath, accessToken);
        ClientResponse response = webResource.uri(ro).path(annotationBodyURIRDF).queryParam("original", "ann1.ttl")
                .header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
        is.close();
        assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
        response.close();
    }


    /**
     * Test that you can add an annotation without the body.
     * 
     * @throws IOException
     *             could not close the test input stream
     */
    @Test
    public void addAnnotation()
            throws IOException {
        addFile(ro, "file1.txt", accessToken);
        InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationGood.rdf");
        ClientResponse response = addAnnotation(is, ro, accessToken);
        is.close();
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
        model.read(response.getEntityInputStream(), null);
        Individual annotation = model.getIndividual(response.getLocation().toString());
        Assert.assertTrue(annotation.hasRDFType(RO.AggregatedAnnotation));
        Assert.assertTrue(model.contains(annotation, AO.body, (RDFNode) null));
        Assert.assertTrue(model.contains(annotation, RO.annotatesAggregatedResource, (RDFNode) null));
        Assert.assertTrue(model.contains(annotation, DCTerms.created, (RDFNode) null));
        Assert.assertTrue(model.contains(annotation, DCTerms.creator, (RDFNode) null));
        response.close();
    }


    /**
     * Test that you cannot add an annotation of a resource that is not aggregated.
     * 
     * @throws IOException
     *             could not close the test input stream
     */
    @Test
    public void addAnnotationWithAnIncorrectTarget()
            throws IOException {
        addFile(ro, "file1.txt", accessToken);
        InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationBad.rdf");
        ClientResponse response = addAnnotation(is, ro, accessToken);
        is.close();
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        response.close();
    }
}
