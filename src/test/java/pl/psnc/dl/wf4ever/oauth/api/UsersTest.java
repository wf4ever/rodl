package pl.psnc.dl.wf4ever.oauth.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletResponse;

import org.junit.Ignore;
import org.junit.Test;

import pl.psnc.dl.wf4ever.W4ETest;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.WebAppDescriptor;

@Ignore
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


    public UsersTest() {
        super(new WebAppDescriptor.Builder("pl.psnc.dl.wf4ever").build());

    }


    @Override
    protected void finalize()
            throws Throwable {
        super.finalize();
    }


    @Test
    public void testUserCreation() {
        ClientResponse response = createUserWithAnswer(userIdSafe, username);
        assertEquals("should be reated", HttpServletResponse.SC_CREATED, response.getStatus());
        response.close();
        response = createUserWithAnswer(userId2Safe, username2);
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        response.close();

        deleteUser(userIdSafe);
        deleteUser(userId2Safe);
    }


    @Test
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
