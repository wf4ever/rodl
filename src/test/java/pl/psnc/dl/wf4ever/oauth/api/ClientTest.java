package pl.psnc.dl.wf4ever.oauth.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.IntegrationTest;
import pl.psnc.dl.wf4ever.W4ETest;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.WebAppDescriptor;

@Category(IntegrationTest.class)
public class ClientTest extends W4ETest {

    private String testClientName = "Test client name";
    private String testClientID;


    public ClientTest() {
        super(new WebAppDescriptor.Builder("pl.psnc.dl.wf4ever").build());

    }


    protected void finalize()
            throws Throwable {
        super.finalize();
    };


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
    public void testClientCreation() {
        ClientResponse response = webResource.path("clients/").header("Authorization", "Bearer " + adminCreds)
                .post(ClientResponse.class, testClientName + "\r\n" + clientRedirectionURI);
        assertEquals(201, response.getStatus());
        URI clientURI = response.getLocation();
        testClientID = clientURI.resolve(".").relativize(clientURI).toString();
        response.close();
        deleteClient(testClientID);
    }


    @Test
    public void testGetCLients() {
        String list = webResource.path("clients/").header("Authorization", "Bearer " + adminCreds).get(String.class);
        assertTrue("clients list should contain client id", list.contains(clientId));
    }


    @Test
    public void testGetClient() {
        String client = webResource.path("clients/" + clientId).header("Authorization", "Bearer " + adminCreds)
                .get(String.class);
        assertTrue("client should be available via get method", client.contains(clientId));
    }
}