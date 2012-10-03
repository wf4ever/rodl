package pl.psnc.dl.wf4ever.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import pl.psnc.dl.wf4ever.W4ETest;

import com.sun.jersey.api.client.ClientResponse;

public class RefactorTest extends W4ETest {

    protected List<String> linkHeadersR = new ArrayList<>();
    private final String externalResource = "http://example.com/external/resource.txt";

    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        createLinkHeaders();
        createUserWithAnswer(userIdSafe, username).close();
        accessToken = createAccessToken(userId);
        ro = createRO(accessToken);
        ro2 = createRO(accessToken);
    }


    @Override
    public void tearDown()
            throws Exception {
        deleteROs();
        deleteAccessToken(accessToken);
        deleteUser(userIdSafe);
        super.tearDown();
    }


    //@Test
    public void testGetROList() {
        String list = webResource.path("ROs").header("Authorization", "Bearer " + accessToken).get(String.class);
        assertTrue(list.contains(ro.toString()));
        assertTrue(list.contains(ro2.toString()));
    }


    //@Test
    public void testGetROWithWhitspaces() {
        URI ro3 = createRO("ro " + UUID.randomUUID().toString(), accessToken);
        String list = webResource.path("ROs").header("Authorization", "Bearer " + accessToken).get(String.class);
        assertTrue(list.contains(ro3.toString()));
    }


    //@Test
    public void testGetROMetadata()
            throws URISyntaxException {
        client().setFollowRedirects(false);
        ClientResponse response = webResource.uri(ro).accept("text/turtle").get(ClientResponse.class);
        assertEquals(HttpServletResponse.SC_SEE_OTHER, response.getStatus());
        assertEquals(webResource.uri(ro).path(".ro/manifest.rdf").getURI().getPath(), response.getLocation().getPath());
        response.close();
    }


    //@Test
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


    //@Test
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


    //@Test
    public void testAggregateExternalResource() {
        String body = String.format("<rdf:RDF xmlns:ore=\"http://www.openarchives.org/ore/terms/\""
                + " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >"
                + "<ore:Proxy> <ore:proxyFor rdf:resource=\"%s\" /> </ore:Proxy> </rdf:RDF>", externalResource);
        ClientResponse response = webResource.uri(ro).header("Authorization", "Bearer " + accessToken)
                .type("application/vnd.wf4ever.proxy").post(ClientResponse.class, body);
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        response.close();
    }
    /*
     * helpers
     */
    private void createLinkHeaders() {
        linkHeadersR.clear();
        linkHeadersR.add("<" + resource().getURI() + "ROs/r/.ro/manifest.rdf>; rel=bookmark");
        linkHeadersR.add("<" + resource().getURI() + "zippedROs/r/>; rel=bookmark");
        linkHeadersR.add("<http://sandbox.wf4ever-project.org/portal/ro?ro=" + resource().getURI()
                + "ROs/r/>; rel=bookmark");
    }
}
