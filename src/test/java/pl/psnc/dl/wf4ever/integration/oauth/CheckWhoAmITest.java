package pl.psnc.dl.wf4ever.integration.oauth;

import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.integration.AbstractIntegrationTest;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

@Category(IntegrationTest.class)
public class CheckWhoAmITest extends AbstractIntegrationTest {

    @Test
    public void checkWhoAmITest() {
        String userIdEncoded = StringUtils.trim(Base64.encodeBase64URLSafeString(userId.getBytes()));
        webResource.path("users/" + userIdEncoded).header("Authorization", "Bearer " + adminCreds)
                .put(ClientResponse.class, "test user");
        String whoami = webResource.path("whoami/").header("Authorization", "Bearer " + accessToken).get(String.class);
        Assert.assertTrue(whoami.contains(userId));
        Assert.assertTrue(whoami.contains("test user"));
    }


    @Test(expected = UniformInterfaceException.class)
    public void checkUnauthorizedWhoIAmQuestion() {
        webResource.path("whoami/").get(Response.class);
    }
}
