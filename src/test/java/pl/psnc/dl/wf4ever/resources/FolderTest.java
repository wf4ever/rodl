package pl.psnc.dl.wf4ever.resources;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.IntegrationTest;
import pl.psnc.dl.wf4ever.vocabulary.ORE;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Tests of manipulating ro:Folders.
 * 
 * @author piotrekhol
 * 
 */
@Category(IntegrationTest.class)
public class FolderTest extends ResourceBase {

    /** folder path. */
    private final String folderPath = "myfolder/";

    /** folder path with spaces. */
    private final String folderWithSpacesPath = "my folder/";

    /** folder path with spaces encoded. */
    private final String folderWithSpacesPathEncoded = "my%20folder/";

    /** file path. */
    private final String filePath = "file2.txt";

    /** a file in the sample folder definition. */
    private final URI f1 = URI.create("https://sandbox/rodl/ROs/ro1/myfolder/file1.txt");

    /** a file in the sample folder definition. */
    private final URI f2 = URI.create("https://sandbox/rodl/ROs/ro1/anotherfolder/file2.txt");

    /** a file in the sample folder definition. */
    private final URI f3 = URI.create("http://example.org");

    /** a file in the sample folder entry definition. */
    private final URI f4 = URI.create("https://sandbox/rodl/ROs/ro1/file2.txt");


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
     * Test for creating a new folder.
     */
    @Test
    public void testAddFolder() {
        addFile(ro, f1, accessToken).close();
        addFile(ro, f2, accessToken).close();
        addFile(ro, f3, accessToken).close();

        InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/folder.rdf");
        ClientResponse response = addFolder(is, ro, folderPath, accessToken);
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        Set<URI> expected = new HashSet<>(Arrays.asList(f1, f2, f3));
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
        model.read(response.getEntityInputStream(), null);
        assertCorrectFolderResourceMap(model, expected);
        URI folderProxyURI = response.getLocation();
        Assert.assertNotNull(folderProxyURI);
        assertCorrectFolderDescription(model, folderProxyURI, ro.resolve(folderPath));
        response.close();
        webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);

        is = getClass().getClassLoader().getResourceAsStream("singleFiles/folder.rdf");
        response = addFolder(is, ro, folderWithSpacesPath, accessToken);
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        model.removeAll();
        model.read(response.getEntityInputStream(), null);
        assertCorrectFolderResourceMap(model, expected);
        folderProxyURI = response.getLocation();
        Assert.assertNotNull(folderProxyURI);
        assertCorrectFolderDescription(model, folderProxyURI, ro.resolve(folderWithSpacesPathEncoded));
        response.close();
        webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
    }


    /**
     * Test for dereferencing a folder.
     */
    @Test
    public void testGetFolder() {
        addFile(ro, f1, accessToken).close();
        addFile(ro, f2, accessToken).close();
        addFile(ro, f3, accessToken).close();

        InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/folder.rdf");
        ClientResponse response = addFolder(is, ro, folderPath, accessToken);
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        URI folderProxyURI = response.getLocation();
        Assert.assertNotNull(folderProxyURI);
        response.close();

        response = webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken)
                .get(ClientResponse.class);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Set<URI> expected = new HashSet<>(Arrays.asList(URI.create("https://sandbox/rodl/ROs/ro1/myfolder/file1.txt"),
            URI.create("https://sandbox/rodl/ROs/ro1/anotherfolder/file2.txt"), URI.create("http://example.org")));
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
        model.read(response.getEntityInputStream(), null);
        assertCorrectFolderResourceMap(model, expected);

        webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
    }


    /**
     * Test for creating a new folder entry.
     */
    @Test
    public void testAddFolderEntry() {
        addFile(ro, f1, accessToken).close();
        addFile(ro, f2, accessToken).close();
        addFile(ro, f3, accessToken).close();
        addFile(ro, f4, accessToken).close();

        addFile(ro, filePath, accessToken);

        InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/folder.rdf");
        ClientResponse response = addFolder(is, ro, folderPath, accessToken);
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        URI folderProxyURI = response.getLocation();
        Assert.assertNotNull(folderProxyURI);
        Multimap<String, URI> links = getLinkHeaders(response.getHeaders().get("Link"));
        response.close();

        URI folderURI = links.get("http://www.openarchives.org/ore/terms/proxyFor").iterator().next();
        is = getClass().getClassLoader().getResourceAsStream("singleFiles/folderEntry.rdf");
        response = addFolderEntry(is, folderURI, accessToken);
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        links = getLinkHeaders(response.getHeaders().get("Link"));
        Assert.assertEquals(URI.create("https://sandbox/rodl/ROs/ro1/file2.txt"),
            links.get("http://www.openarchives.org/ore/terms/proxyFor").iterator().next());
        response.close();

        response = webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken)
                .get(ClientResponse.class);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        Set<URI> expected = new HashSet<>(Arrays.asList(URI.create("https://sandbox/rodl/ROs/ro1/myfolder/file1.txt"),
            URI.create("https://sandbox/rodl/ROs/ro1/anotherfolder/file2.txt"), URI.create("http://example.org"),
            URI.create("https://sandbox/rodl/ROs/ro1/file2.txt")));
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
        model.read(response.getEntityInputStream(), null);
        assertCorrectFolderResourceMap(model, expected);
        response.close();

        webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
    }


    /**
     * Test for deleting a folder entry.
     */
    @Test
    public void testDeleteFolderEntry() {
        addFile(ro, f1, accessToken).close();
        addFile(ro, f2, accessToken).close();
        addFile(ro, f3, accessToken).close();

        addFile(ro, filePath, accessToken);

        InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/folder.rdf");
        ClientResponse response = addFolder(is, ro, folderPath, accessToken);
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        URI folderProxyURI = response.getLocation();
        Assert.assertNotNull(folderProxyURI);
        response.close();

        response = webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken)
                .get(ClientResponse.class);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        URI folderEntry = getFolderEntryURI(response.getEntityInputStream(),
            URI.create("https://sandbox/rodl/ROs/ro1/anotherfolder/file2.txt"));
        response.close();

        response = webResource.uri(folderEntry).header("Authorization", "Bearer " + accessToken)
                .delete(ClientResponse.class);
        assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatus());
        response.close();

        response = webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken)
                .get(ClientResponse.class);
        Set<URI> expected = new HashSet<>(Arrays.asList(URI.create("https://sandbox/rodl/ROs/ro1/myfolder/file1.txt"),
            URI.create("http://example.org")));
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
        model.read(response.getEntityInputStream(), null);
        assertCorrectFolderResourceMap(model, expected);
        response.close();

        webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
    }


    /**
     * Test for deleting a folder.
     */
    @Test
    public void testDeleteFolder() {
        addFile(ro, f1, accessToken).close();
        addFile(ro, f2, accessToken).close();
        addFile(ro, f3, accessToken).close();

        InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/folder.rdf");
        ClientResponse response = addFolder(is, ro, folderPath, accessToken);
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        URI folderProxyURI = response.getLocation();
        Assert.assertNotNull(folderProxyURI);
        response.close();

        response.getClient().setFollowRedirects(false);
        response = webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken)
                .get(ClientResponse.class);
        response = webResource.uri(response.getLocation()).header("Authorization", "Bearer " + accessToken)
                .get(ClientResponse.class);
        URI resourceMap = response.getLocation();
        response.getClient().setFollowRedirects(true);
        response = webResource.uri(resourceMap).header("Authorization", "Bearer " + accessToken)
                .get(ClientResponse.class);
        assertEquals(HttpStatus.SC_OK, response.getStatus());

        response = webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken)
                .delete(ClientResponse.class);
        assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatus());

        response = webResource.uri(resourceMap).header("Authorization", "Bearer " + accessToken)
                .get(ClientResponse.class);
        assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatus());
    }


    /**
     * Check if folder resource map is correct.
     * 
     * @param model
     *            RDF model
     * @param proxyFors
     *            list of aggregated resources
     */
    private void assertCorrectFolderResourceMap(OntModel model, Set<URI> proxyFors) {
        List<Individual> folders = model.listIndividuals(RO.Folder).toList();
        Assert.assertEquals(1, folders.size());
        List<RDFNode> folderEntries = folders.get(0).listPropertyValues(ORE.aggregates).toList();
        Assert.assertEquals(proxyFors.size(), folderEntries.size());

        List<Individual> entries = model.listIndividuals(RO.FolderEntry).toList();
        Assert.assertEquals(proxyFors.size(), entries.size());
        Set<URI> entryProxyFors = new HashSet<>();
        for (Individual entry : entries) {
            Assert.assertTrue(entry.hasProperty(RO.entryName));
            Assert.assertTrue(entry.hasProperty(ORE.proxyFor));
            Assert.assertTrue(entry.hasProperty(ORE.proxyIn));
            entryProxyFors.add(URI.create(entry.getPropertyResourceValue(ORE.proxyFor).getURI()));
        }
        Assert.assertEquals(proxyFors, entryProxyFors);
    }


    /**
     * Check if the model contains information from the manifest about the folder.
     * 
     * @param model
     *            RDF model
     * @param proxyUri
     *            folder proxy URI
     * @param folderUri
     *            folder URI
     */
    private void assertCorrectFolderDescription(OntModel model, URI proxyUri, URI folderUri) {
        Individual proxy = model.getIndividual(proxyUri.toString());
        Individual resource = model.getIndividual(folderUri.toString());
        Assert.assertNotNull(proxy);
        Assert.assertNotNull(resource);
        Assert.assertTrue(proxy.hasRDFType(ORE.Proxy));
        Assert.assertTrue(resource.hasRDFType(RO.Folder));
        Assert.assertTrue(model.contains(proxy, ORE.proxyFor, resource));
        Assert.assertTrue(model.contains(resource, DCTerms.created, (RDFNode) null));
        Assert.assertTrue(model.contains(resource, DCTerms.creator, (RDFNode) null));
    }


    /**
     * Find a folder entry URI given a resource.
     * 
     * @param entityInputStream
     *            input stream with the RDF/XML resource map
     * @param proxyFor
     *            the URI of the aggregated resource
     * @return entry URI
     */
    private URI getFolderEntryURI(InputStream entityInputStream, URI proxyFor) {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
        model.read(entityInputStream, null);

        Resource proxyForR = model.createResource(proxyFor.toString());
        List<Resource> entries = model.listSubjectsWithProperty(ORE.proxyFor, proxyForR).toList();
        Assert.assertEquals(1, entries.size());
        return URI.create(entries.get(0).getURI());
    }
}
