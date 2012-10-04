package pl.psnc.dl.wf4ever.oauth.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.WebAppDescriptor;

import pl.psnc.dl.wf4ever.W4ETest;

public class AccessTokenTest extends W4ETest {

    private static String testAccessToken;
    
    public AccessTokenTest(){
        super(new WebAppDescriptor.Builder("pl.psnc.dl.wf4ever").build());
        createUserWithAnswer(userIdSafe, username).close();
    }
    
    @Override
    protected void finalize()
            throws Throwable {
        deleteUser(userIdSafe);
        super.finalize();
    }
    
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
    public void testAccessTokenCreation() {
        ClientResponse response = webResource.path("accesstokens/").header("Authorization", "Bearer " + adminCreds)
                .post(ClientResponse.class, clientId + "\r\n" + userId);
        assertEquals("Access token should be cerated correctly",HttpServletResponse.SC_CREATED, response.getStatus());
        deleteAccessToken(response.getLocation().resolve(".").relativize(response.getLocation()).toString());
        response.close();
    }
    
    //@Test
    public void testGetAccessToken(){
        testAccessToken = createAccessToken(userId);
        String list = webResource.path("accesstokens/").header("Authorization", "Bearer " + adminCreds)
                .get(String.class);
        assertTrue("Access Token should be available",list.contains(testAccessToken));
        deleteAccessToken(testAccessToken);
    }

}