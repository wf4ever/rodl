package pl.psnc.dl.wf4ever.resources;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.Test;

import pl.psnc.dl.wf4ever.vocabulary.ORE;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Tests of manipulating ro:Folders.
 * 
 * @author piotrekhol
 * 
 */
public class FolderTest extends ResourceBase {

    /** folder path. */
    private final String folderPath = "myfolder/";


    @Override
    public void setUp()
            throws Exception {
        super.setUp();
    }


    @Override
    public void tearDown()
            throws Exception {
    }


    /**
     * Test for creating a new folder.
     */
    @Test
    public void addFolder() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("folder.rdf");
        ClientResponse response = addFolder(is, ro, folderPath, accessToken);
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        assertCorrectFolderResourceMap(response.getEntityInputStream());
        URI folderProxyURI = response.getLocation();
        Assert.assertNotNull(folderProxyURI);
        response.close();
        webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
    }


    /**
     * Test for dereferencing a folder.
     */
    @Test
    public void getFolder() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("folder.rdf");
        ClientResponse response = addFolder(is, ro, folderPath, accessToken);
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        URI folderProxyURI = response.getLocation();
        Assert.assertNotNull(folderProxyURI);
        response.close();

        response = webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken)
                .get(ClientResponse.class);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertCorrectFolderResourceMap(response.getEntityInputStream());

        webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
    }


    /**
     * Check if folder resource map is correct.
     * 
     * @param entityInputStream
     *            input stream with the RDF/XML resource map
     */
    private void assertCorrectFolderResourceMap(InputStream entityInputStream) {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
        model.read(entityInputStream, null);

        List<Individual> folders = model.listIndividuals(RO.Folder).toList();
        Assert.assertEquals(1, folders.size());
        List<RDFNode> folderEntries = folders.get(0).listPropertyValues(ORE.aggregates).toList();
        Assert.assertEquals(3, folderEntries.size());

        List<Individual> entries = model.listIndividuals(RO.FolderEntry).toList();
        Assert.assertEquals(3, entries.size());
        for (Individual entry : entries) {
            Assert.assertTrue(entry.hasProperty(RO.entryName));
            Assert.assertTrue(entry.hasProperty(ORE.proxyFor));
            Assert.assertTrue(entry.hasProperty(ORE.proxyIn));
        }
    }

}
