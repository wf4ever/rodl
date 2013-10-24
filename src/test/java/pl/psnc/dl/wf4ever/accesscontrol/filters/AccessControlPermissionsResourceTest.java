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
import pl.psnc.dl.wf4ever.accesscontrol.model.Permission;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.sun.jersey.api.client.ClientResponse;

@Category(IntegrationTest.class)
public class AccessControlPermissionsResourceTest extends AccessControlTest {

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
        //check which permissions were registered
        Permission[] permissions = webResource.path("accesscontrol/permissions/")
                .queryParam("ro", createdRO.toString()).header("Authorization", "Bearer " + adminCreds)
                .accept(MediaType.APPLICATION_JSON).get(Permission[].class);
        Assert.assertEquals(permissions.length, 1);
        Permission permission = permissions[0];
        //query about this one permission and check who can read it

        ClientResponse response = queryPermissions(accessToken, createdRO.toString());
        Assert.assertEquals(200, response.getStatus());
        response = queryPermissions(adminCreds, createdRO.toString());
        Assert.assertEquals(200, response.getStatus());
        response = queryPermissions(accessToken2, createdRO.toString());
        Assert.assertEquals(403, response.getStatus());

        response = getPermission(accessToken, permission.getUri());
        Assert.assertEquals(200, response.getStatus());
        response = getPermission(adminCreds, permission.getUri());
        Assert.assertEquals(200, response.getStatus());
        response = getPermission(accessToken2, permission.getUri());
        Assert.assertEquals(403, response.getStatus());

        response = grantPermission(accessToken2, createdRO, Role.EDITOR, userProfile2);
        Assert.assertEquals(400, response.getStatus());
        response = grantPermission(accessToken, createdRO, Role.EDITOR, userProfile2);
        Assert.assertEquals(201, response.getStatus());

        Permission grantedPermission = response.getEntity(Permission.class);

        response = delete(grantedPermission.getUri(), accessToken2);
        Assert.assertEquals(403, response.getStatus());
        response = delete(grantedPermission.getUri(), accessToken);
        Assert.assertEquals(204, response.getStatus());

    }


    private ClientResponse queryPermissions(String token, String ro) {
        return webResource.path("accesscontrol/permissions/").queryParam("ro", ro)
                .header("Authorization", "Bearer " + token).accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);

    }


    private ClientResponse getPermission(String token, URI uri) {
        return webResource.uri(uri).header("Authorization", "Bearer " + token).accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
    }

}
