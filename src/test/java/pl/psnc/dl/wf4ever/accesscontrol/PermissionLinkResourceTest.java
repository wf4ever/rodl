package pl.psnc.dl.wf4ever.accesscontrol;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.accesscontrol.dicts.Role;
import pl.psnc.dl.wf4ever.accesscontrol.model.PermissionLink;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.sun.jersey.api.client.ClientResponse;

@Category(IntegrationTest.class)
public class PermissionLinkResourceTest extends AccessControlTest {

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
    public void testIfPermissionIsSetOnceTheROisCreatedAndDeletedWithRO()
            throws InterruptedException {
        URI createdRO = createRO();
        PermissionLink[] permissions = webResource.path("accesscontrol/permissionlinks/")
                .queryParam("ro", createdRO.toString()).header("Authorization", "Bearer " + adminCreds)
                .accept(MediaType.APPLICATION_JSON).get(PermissionLink[].class);

        Assert.assertEquals(permissions.length, 0);

        ClientResponse response = grantPermissionLink(createdRO, Role.REDAER, userProfile);
        PermissionLink serverPermission = response.getEntity(PermissionLink.class);
        Assert.assertEquals(response.getLocation(), serverPermission.getUri());
        Assert.assertEquals(201, response.getStatus());

        permissions = webResource.path("accesscontrol/permissionlinks/").queryParam("ro", createdRO.toString())
                .header("Authorization", "Bearer " + adminCreds).accept(MediaType.APPLICATION_JSON)
                .get(PermissionLink[].class);
        Assert.assertEquals(1, permissions.length);

        //conflict
        response = grantPermissionLink(createdRO, Role.REDAER, userProfile);
        Assert.assertEquals(409, response.getStatus());
        delete(createdRO, adminCreds);

        permissions = webResource.path("accesscontrol/permissionlinks/").queryParam("ro", createdRO.toString())
                .header("Authorization", "Bearer " + adminCreds).accept(MediaType.APPLICATION_JSON)
                .get(PermissionLink[].class);
        Assert.assertEquals(permissions.length, 0);
    }


    @Test
    public void testReaderWriterAndAuthorShoulBeAbleToReadRO() {
        URI createdRO = createRO(accessToken);
        ClientResponse response = webResource.uri(createdRO).header("Authorization", "Bearer " + accessToken)
                .get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus());
        grantPermission(createdRO, Role.REDAER, userProfile2);
        response = webResource.uri(createdRO).accept("application/json")
                .header("Authorization", "Bearer " + accessToken2).get(ClientResponse.class);
        grantPermission(createdRO, Role.REDAER, userProfile2);
        response = webResource.uri(createdRO).accept("application/json")
                .header("Authorization", "Bearer " + accessToken2).get(ClientResponse.class);
        response = webResource.uri(createdRO).accept("application/json")
                .header("Authorization", "Bearer " + accessToken2).get(ClientResponse.class);
    }

}
