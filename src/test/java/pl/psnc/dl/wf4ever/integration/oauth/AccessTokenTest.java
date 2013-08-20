package pl.psnc.dl.wf4ever.integration.oauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.integration.AbstractIntegrationTest;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.sun.jersey.api.client.ClientResponse;

@Category(IntegrationTest.class)
public class AccessTokenTest extends AbstractIntegrationTest {

    @Test
    public void testAccessTokenCreation() {
        ClientResponse response = webResource.path("accesstokens/").header("Authorization", "Bearer " + adminCreds)
                .post(ClientResponse.class, clientId + "\r\n" + userId);
        assertEquals("Access token should be created correctly", HttpServletResponse.SC_CREATED, response.getStatus());
        response.close();
    }


    @Test
    public void testGetAccessToken() {
        String list = webResource.path("accesstokens/").header("Authorization", "Bearer " + adminCreds)
                .get(String.class);
        assertTrue("Access Token should be available", list.contains(accessToken));
    }

}