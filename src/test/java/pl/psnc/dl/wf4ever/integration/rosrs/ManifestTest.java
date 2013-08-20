package pl.psnc.dl.wf4ever.integration.rosrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.URI;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.sun.jersey.api.client.ClientResponse;

@Category(IntegrationTest.class)
public class ManifestTest extends RosrsTest {

    private final String filePath = "foo/bar ra.txt";
    private final String filePathEncoded = "foo/bar%20ra.txt";
    private final String rdfFilePath = "foo/bar.rdf";
    private String rdfFileBody = "<rdf:RDF" + "  xmlns:ore=\"http://www.openarchives.org/ore/terms/\" \n"
            + "   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >\n" + "   <ore:Proxy>\n"
            + "   </ore:Proxy>\n" + " </rdf:RDF>";
    private URI rdfProxy;


    @Test
    public void testUpdateManifest() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("manifest.ttl");
        ClientResponse response = webResource.uri(ro).path("/.ro/manifest.rdf")
                .header("Authorization", "Bearer " + accessToken).type("text/turtle").put(ClientResponse.class, is);
        assertEquals("Updating manifest should be protected", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        response.close();
    }


    @Test
    public void getManifest() {
        addLoremIpsumFile(ro, filePath);
        rdfProxy = addFile(ro, rdfFilePath, IOUtils.toInputStream(rdfFileBody), "application/rdf+xml").getLocation();
        String manifest = webResource.uri(ro).path("/.ro/manifest.rdf")
                .header("Authorization", "Bearer " + accessToken).get(String.class);
        assertTrue("Manifest should contain user id", manifest.contains(userId));
        assertTrue("Manifest should contain file path", manifest.contains(filePathEncoded));
        assertTrue("Manifest should contain rdf proxy", manifest.contains(rdfProxy.toString()));

        manifest = webResource.uri(ro).path("/.ro/manifest.rdf").header("Authorization", "Bearer " + accessToken)
                .accept("application/x-turtle").get(String.class);
        assertTrue("Manifest should contain user id", manifest.contains(userId));
        assertTrue("Manifest should contain file path", manifest.contains(filePathEncoded));

        manifest = webResource.uri(ro).path("/.ro/manifest.n3").queryParam("original", "manifest.rdf")
                .header("Authorization", "Bearer " + accessToken).get(String.class);
        assertTrue("Manifest should contain user id", manifest.contains(userId));
        assertTrue("Manifest should contain user id", manifest.contains(filePathEncoded));

        ClientResponse response = webResource.uri(ro).path("/.ro/manifest.n3")
                .header("Authorization", "Bearer " + accessToken).get(ClientResponse.class);
        assertEquals("Should return 404 for manifest.n3", HttpStatus.SC_NOT_FOUND, response.getStatus());
        response.close();
    }


    @Test
    public void getInitialManifest() {
        String manifest = webResource.uri(ro).path("/.ro/manifest.rdf")
                .header("Authorization", "Bearer " + accessToken).get(String.class);
        assertFalse("Empty manifest should not contain file path", manifest.contains(filePathEncoded));

        manifest = webResource.uri(ro).path("/.ro/manifest.rdf").header("Authorization", "Bearer " + accessToken)
                .accept("application/x-turtle").get(String.class);
        assertFalse("Empty manifest should not contain user id", manifest.contains(filePathEncoded));

        //        ClientResponse response = webResource.uri(ro).path("/.ro/manifest.rdf")
        //                .header("Authorization", "Bearer " + accessToken).get(ClientResponse.class);
        //        assertEquals(linkHeadersR, response.getHeaders().get("Link"));
        //        response.close();
    }


    @Test
    public void getManifestWithAnnotationBody() {
        //TODO
        //How to handle TriG ?!?!?!
        addLoremIpsumFile(ro, filePath);
        rdfProxy = addFile(ro, rdfFilePath, IOUtils.toInputStream(rdfFileBody), "application/rdf+xml").getLocation();
        String manifest = webResource.uri(ro).path("/.ro/manifest.rdf")
                .header("Authorization", "Bearer " + accessToken).accept("application/x-turtle").get(String.class);
        assertTrue("Manifest should contain user id", manifest.contains(userId));
        assertTrue("Manifest should contain user id", manifest.contains(filePathEncoded));
        //assertTrue("Annotation body should contain file path: " + filePath, manifest.contains("a_workflow.t2flow"));
        //assertTrue(manifest.contains("A test"));

        manifest = webResource.uri(ro).path("/.ro/manifest.ttl").queryParam("original", "manifest.rdf")
                .header("Authorization", "Bearer " + accessToken).get(String.class);
        assertTrue("Manifest should contain user id", manifest.contains(userId));
        assertTrue("Manifest should contain user id", manifest.contains(filePathEncoded));
        //assertTrue("Annotation body should contain file path: " + filePathEncoded,
        //    manifest.contains("a_workflow.t2flow"));
        //assertTrue(manifest.contains("A test"));
    }

}
