package pl.psnc.dl.wf4ever.oauth.api;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.test.framework.WebAppDescriptor;

import junit.framework.Assert;
import pl.psnc.dl.wf4ever.W4ETest;

public class CheckWhoAmITest extends W4ETest {

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


    public CheckWhoAmITest() {
        super(new WebAppDescriptor.Builder("pl.psnc.dl.wf4ever").build());
        createUserWithAnswer(userIdSafe, username).close();
        accessToken = createAccessToken(userId);
    }


    @Override
    protected void finalize()
            throws Throwable {
        deleteAccessToken(accessToken);
        deleteUser(userIdSafe);
        super.finalize();
    }


    //@Test
    public void checkWhoAmITest() {
        String whoami = webResource.path("whoami/").header("Authorization", "Bearer " + accessToken).get(String.class);
        Assert.assertTrue(whoami.contains(userId));
        Assert.assertTrue(whoami.contains(username));
    }


    //@Test(expected=UniformInterfaceException.class)
    public void checkUnauthorizedWhoIAmQuestion() {
        webResource.path("whoami/").get(Response.class);
    }
}