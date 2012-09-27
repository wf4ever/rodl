package pl.psnc.dl.wf4ever;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;


public class APITest extends JerseyTest {

    protected WebResource webResource;
    protected final String adminCreds = StringUtils.trim(Base64.encodeBase64String("wfadmin:wfadmin!!!".getBytes()));
    protected final String clientName = "ROSRS testing app written in Ruby";
    protected final String clientRedirectionURI = "OOB"; // will not be used
    protected String clientId;
    protected final String userId2 = UUID.randomUUID().toString();
    protected final String userId = UUID.randomUUID().toString();
    protected final String userIdUrlSafe = StringUtils.trim(Base64.encodeBase64URLSafeString(userId.getBytes()));
    protected final String userId2UrlSafe = StringUtils.trim(Base64.encodeBase64URLSafeString(userId2.getBytes()));
    protected final String username = "John Doe";
    protected final String username2 = "May Gray";
    
    public APITest() {
        super(new WebAppDescriptor.Builder("pl.psnc.dl.wf4ever").build());
    }
    public APITest(WebAppDescriptor webAppDescriptor){
        super(new WebAppDescriptor.Builder("pl.psnc.dl.wf4ever").build());
    }
    
    @Override
    public void setUp() throws Exception{
        super.setUp();
        client().setFollowRedirects(true);
        if (resource().getURI().getHost().equals("localhost")) {
            webResource = resource();
        } else {
            webResource = resource().path("rodl/");
        }
        clientId = createClient();
    }
    
    protected String createClient() {
        ClientResponse response = webResource.path("clients/").header("Authorization", "Bearer " + adminCreds)
                .post(ClientResponse.class, clientName + "\r\n" + clientRedirectionURI);
        String clientId = response.getLocation().resolve(".").relativize(response.getLocation()).toString();
        response.close();
        return clientId;
    }
    
    protected void createUsers() {
        ClientResponse response = webResource.path("users/" + userIdUrlSafe)
                .header("Authorization", "Bearer " + adminCreds).put(ClientResponse.class, username);
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        response.close();

        response = webResource.path("users/" + userId2UrlSafe).header("Authorization", "Bearer " + adminCreds)
                .put(ClientResponse.class, username2);
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        response.close();
    }
    
    protected void getUser() {
        String user = webResource.path("users/" + userIdUrlSafe).header("Authorization", "Bearer " + adminCreds)
                .get(String.class);
        assertTrue(user.contains(userId));

        user = webResource.path("users/" + userId2UrlSafe).header("Authorization", "Bearer " + adminCreds)
                .get(String.class);
        assertTrue(user.contains(userId2));

        user = webResource.path("users/" + userId2UrlSafe).header("Authorization", "Bearer " + adminCreds)
                .accept("application/x-turtle").get(String.class);
        assertTrue(user.contains(userId2));
    }
    
    protected String createAccessToken(String userId) {
        ClientResponse response = webResource.path("accesstokens/").header("Authorization", "Bearer " + adminCreds)
                .post(ClientResponse.class, clientId + "\r\n" + userId);
        String accessToken = response.getLocation().resolve(".").relativize(response.getLocation()).toString();
        response.close();
        return accessToken;
    }
    
    protected URI createRO(String accessToken) {
        String uuid = UUID.randomUUID().toString();
        return createRO(uuid, accessToken);
    }


    protected URI createRO(String uuid, String accessToken) {
        ClientResponse response = webResource.path("ROs/").header("Authorization", "Bearer " + accessToken)
                .header("Slug", uuid).post(ClientResponse.class);
        URI ro = response.getLocation();
        response.close();
        return ro;
    }

}
