package pl.psnc.dl.wf4ever.accesscontrol.filters;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.accesscontrol.AccessControlTest;
import pl.psnc.dl.wf4ever.accesscontrol.dicts.Role;
import pl.psnc.dl.wf4ever.accesscontrol.model.PermissionLink;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.sun.jersey.api.client.ClientResponse;

@Category(IntegrationTest.class)
public class AccessControlPermissionsLinkResourceTest extends AccessControlTest {

    @Override
    @Before
    public void setUp()
            throws Exception {
        super.setUp();
    }


    @Override
    @After
    public void tearDown()
            throws Exception {
        super.tearDown();
    }


    @Test
    public void testIfOnlyOnwerCanReadAndWriteModes() {
        URI createdRO = createRO(accessToken);
        //only ro owner can grant permissionslink
        ClientResponse response = grantPermissionLink(accessToken2, createdRO, Role.EDITOR, userProfile2);
        Assert.assertEquals(400, response.getStatus());
        response = grantPermissionLink(accessToken, createdRO, Role.EDITOR, userProfile2);
        Assert.assertEquals(201, response.getStatus());
        PermissionLink[] permissions = webResource.path("accesscontrol/permissionlinks/")
                .queryParam("ro", createdRO.toString()).header("Authorization", "Bearer " + adminCreds)
                .accept(MediaType.APPLICATION_JSON).get(PermissionLink[].class);
        Assert.assertEquals(permissions.length, 1);
        PermissionLink permissionLink = permissions[0];

        response = queryPermissionLinks(accessToken, createdRO.toString());
        Assert.assertEquals(200, response.getStatus());
        response = queryPermissionLinks(adminCreds, createdRO.toString());
        Assert.assertEquals(200, response.getStatus());
        response = queryPermissionLinks(accessToken2, createdRO.toString());
        Assert.assertEquals(403, response.getStatus());

        response = getPermissionLink(accessToken, permissionLink.getUri());
        Assert.assertEquals(200, response.getStatus());
        response = getPermissionLink(adminCreds, permissionLink.getUri());
        Assert.assertEquals(200, response.getStatus());
        response = getPermissionLink(accessToken2, permissionLink.getUri());
        Assert.assertEquals(200, response.getStatus());

        response = delete(permissionLink.getUri(), accessToken2);
        Assert.assertEquals(403, response.getStatus());
        response = delete(permissionLink.getUri(), accessToken);
        Assert.assertEquals(204, response.getStatus());
    }


    private ClientResponse queryPermissionLinks(String token, String ro) {
        return webResource.path("accesscontrol/permissionlinks/").queryParam("ro", ro)
                .header("Authorization", "Bearer " + token).accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);

    }


    private ClientResponse getPermissionLink(String token, URI uri) {
        return webResource.uri(uri).header("Authorization", "Bearer " + token).accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
    }

}
