package pl.psnc.dl.wf4ever.oauth.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;

import pl.psnc.dl.wf4ever.W4ETest;

public class UsersTest extends W4ETest {

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


    //@Test
    public void testUserCreation() {
        ClientResponse response = createUserWithAnswer(userIdSafe, username);
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        response.close();

        response = createUserWithAnswer(userId2Safe, username2);
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        response.close();

        deleteUser(userIdSafe);
        deleteUser(userId2Safe);
    }


    //@Test
    public void testGetUser() {
        
        ClientResponse response = createUserWithAnswer(userIdSafe, username);
        ClientResponse response2 = createUserWithAnswer(userId2Safe, username2);
        
        String user = webResource.path("users/" + userIdSafe).header("Authorization", "Bearer " + adminCreds)
                .get(String.class);
        assertTrue(user.contains(userId));

        user = webResource.path("users/" + userId2Safe).header("Authorization", "Bearer " + adminCreds)
                .get(String.class);
        assertTrue(user.contains(userId2));

        user = webResource.path("users/" + userId2Safe).header("Authorization", "Bearer " + adminCreds)
                .accept("application/x-turtle").get(String.class);
        assertTrue(user.contains(userId2));
        
        response.close();
        response2.close();
        
        deleteUser(userIdSafe);
        deleteUser(userId2Safe);
    }

}
