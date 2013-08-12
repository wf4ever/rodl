package pl.psnc.dl.wf4ever.integration.oauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.integration.AbstractIntegrationTest;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.sun.jersey.api.client.ClientResponse;

@Category(IntegrationTest.class)
public class UsersTest extends AbstractIntegrationTest {

    /** Random user ID. */
    private String user1;

    /** Random user ID. */
    private String user2;


    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        user1 = "http://" + UUID.randomUUID().toString();
        user2 = "http://" + UUID.randomUUID().toString();
    }


    @Test
    public void testUserCreation() {
        ClientResponse response = createUser(user1, "User 1");
        assertEquals("should be reated", HttpServletResponse.SC_CREATED, response.getStatus());
        response.close();
        response = createUser(user2, "User 2");
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        response.close();

        deleteUser(user1);
        deleteUser(user2);
    }


    @Test
    public void testGetUser() {
        ClientResponse response = createUser(user1, "User 1");
        ClientResponse response2 = createUser(user2, "User 2");

        String user1Encoded = StringUtils.trim(Base64.encodeBase64URLSafeString(user1.getBytes()));
        String user = webResource.path("users/" + user1Encoded).header("Authorization", "Bearer " + adminCreds)
                .get(String.class);
        assertTrue(user.contains(user1));

        user = webResource.path("users/" + user1Encoded).header("Authorization", "Bearer " + adminCreds)
                .accept("application/x-turtle").get(String.class);
        assertTrue(user.contains(user1));

        response.close();
        response2.close();

        deleteUser(user1);
        deleteUser(user2);
    }


    private ClientResponse createUser(String userId, String username) {
        String userIdEncoded = StringUtils.trim(Base64.encodeBase64URLSafeString(userId.getBytes()));
        return webResource.path("users/" + userIdEncoded).header("Authorization", "Bearer " + adminCreds)
                .put(ClientResponse.class, username);
    }


    private void deleteUser(String userId) {
        String userIdEncoded = StringUtils.trim(Base64.encodeBase64URLSafeString(userId.getBytes()));
        webResource.path("users/" + userIdEncoded).header("Authorization", "Bearer " + adminCreds)
                .delete(ClientResponse.class).close();
    }

}
