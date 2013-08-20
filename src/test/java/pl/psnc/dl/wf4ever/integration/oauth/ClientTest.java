package pl.psnc.dl.wf4ever.integration.oauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.integration.AbstractIntegrationTest;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.sun.jersey.api.client.ClientResponse;

@Category(IntegrationTest.class)
public class ClientTest extends AbstractIntegrationTest {

    private String testClientName = "Test client name";
    private String testClientID;


    @Test
    public void testClientCreation() {
        ClientResponse response = webResource.path("clients/").header("Authorization", "Bearer " + adminCreds)
                .post(ClientResponse.class, testClientName + "\r\n" + CLIENT_REDIRECTION_URI);
        assertEquals(201, response.getStatus());
        URI clientURI = response.getLocation();
        testClientID = clientURI.resolve(".").relativize(clientURI).toString();
        response.close();
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