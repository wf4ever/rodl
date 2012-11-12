package pl.psnc.dl.wf4ever.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import pl.psnc.dl.wf4ever.common.ResearchObject;
import pl.psnc.dl.wf4ever.common.UserProfile;
import pl.psnc.dl.wf4ever.common.UserProfile.Role;
import pl.psnc.dl.wf4ever.exceptions.ManifestTraversingException;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

public class ResourceTest extends ResourceBase {

    protected String createdFromZipResourceObject = UUID.randomUUID().toString();


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


    @Test
    public void testGetROList() {
        String list = webResource.path("ROs").header("Authorization", "Bearer " + accessToken).get(String.class);
        assertTrue(list.contains(ro.toString()));
        assertTrue(list.contains(ro2.toString()));
    }


    @Test
    public void testGetROWithWhitspaces() {
        URI ro3 = createRO("ro " + UUID.randomUUID().toString(), accessToken);
        String list = webResource.path("ROs").header("Authorization", "Bearer " + accessToken).get(String.class);
        assertTrue(list.contains(ro3.toString()));
    }


    @Test
    public void testGetROMetadata()
            throws URISyntaxException {
        client().setFollowRedirects(false);
        ClientResponse response = webResource.uri(ro).accept("text/turtle").get(ClientResponse.class);
        assertEquals(HttpServletResponse.SC_SEE_OTHER, response.getStatus());
        assertEquals(webResource.uri(ro).path(".ro/manifest.rdf").getURI().getPath(), response.getLocation().getPath());
        response.close();
    }


    @Test
    public void testGetROHTML()
            throws URISyntaxException {
        client().setFollowRedirects(false);
        ClientResponse response = webResource.uri(ro).path("/").accept("text/html").get(ClientResponse.class);
        assertEquals(HttpServletResponse.SC_SEE_OTHER, response.getStatus());
        URI portalURI = new URI("http", "sandbox.wf4ever-project.org", "/portal/ro", "ro="
                + webResource.uri(ro).getURI().toString(), null);
        assertEquals(portalURI.getPath(), response.getLocation().getPath());
        assertTrue(portalURI.getQuery().contains("ro="));
        response.close();
    }


    @Test
    public void testGetROZip() {
        client().setFollowRedirects(false);
        ClientResponse response = webResource.uri(ro).accept("application/zip").get(ClientResponse.class);
        assertEquals(HttpServletResponse.SC_SEE_OTHER, response.getStatus());
        assertEquals(webResource.path("zippedROs").path(ro.toString().split("ROs")[1]).getURI().getPath(), response
                .getLocation().getPath());
        response.close();

        response = webResource.path("zippedROs").path(ro.toString().split("ROs")[1]).get(ClientResponse.class);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/zip", response.getType().toString());
        response.close();

        response = webResource.path("zippedROs").path(ro.toString().split("ROs")[1])
                .accept("text/html;q=0.9,*/*;q=0.8").get(ClientResponse.class);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/zip", response.getType().toString());
        response.close();
    }


    @Test
    public void createROFromZip()
            throws IOException, ManifestTraversingException, ClassNotFoundException, NamingException, SQLException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("ro1.zip");
        ClientResponse response = webResource.path("ROs").accept("text/turtle")
                .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                .type("application/zip").post(ClientResponse.class, is);
        assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
            response.getStatus());

        //assertTrue("manifest should contain ann1-body", manifest.contains("/.ro/ann1-body.ttl"));
        //assertTrue("manifest should contain ann-blank", manifest.contains("/.ro/ann-blank.ttl"));

        //assertTrue("manifest should contain res1", manifest.contains("/res1"));
        //assertTrue("manifest should contain afinalfolder", manifest.contains("/afinalfolder"));
        //assertTrue("manifest should contain res2", manifest.contains("/res2"));
        //String fileContent = getResourceToString(ResearchObject.create(response.getLocation()), "res1");

        //assertTrue("res1 should contain lorem ipsum", fileContent.contains("lorem ipsum"));
        SemanticMetadataService sms = new SemanticMetadataServiceImpl(UserProfile.create("login", "name", Role.ADMIN));
        List<Annotation> annotations = sms.getAnnotations(ResearchObject.create(response.getLocation()));
        //assertEquals("research object should contan two nnotations", annotations.size(), 2);
        response.close();
    }


    @Test
    public void createConflictedROFromZip()
            throws UniformInterfaceException, ClientHandlerException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("ro1.zip");
        ClientResponse response = webResource.path("ROs").accept("text/turtle")
                .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                .type("application/zip").post(ClientResponse.class, IOUtils.toByteArray(is));
        assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
            response.getStatus());
        is = getClass().getClassLoader().getResourceAsStream("ro1.zip");
        response = webResource.path("ROs").accept("text/turtle").header("Authorization", "Bearer " + accessToken)
                .header("Slug", createdFromZipResourceObject).type("application/zip")
                .post(ClientResponse.class, IOUtils.toByteArray(is));
        assertEquals("Research objects with this same name should be conflicted", HttpServletResponse.SC_CONFLICT,
            response.getStatus());

    }
}
