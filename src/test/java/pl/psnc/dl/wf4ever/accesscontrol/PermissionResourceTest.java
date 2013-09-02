package pl.psnc.dl.wf4ever.accesscontrol;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.accesscontrol.dicts.Role;
import pl.psnc.dl.wf4ever.accesscontrol.model.Permission;
import pl.psnc.dl.wf4ever.db.UserProfile;
import pl.psnc.dl.wf4ever.integration.AbstractIntegrationTest;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.sun.jersey.api.client.ClientResponse;

@Category(IntegrationTest.class)
public class PermissionResourceTest extends AbstractIntegrationTest {

    protected final String userId2 = "http://" + UUID.randomUUID().toString();
    protected String accessToken2;


    @Override
    @Before
    public void setUp()
            throws Exception {
        super.setUp();
        ClientResponse response = createUser(userId2, "test user");
        response = createAccessToken(clientId, userId2);
        accessToken2 = response.getLocation().resolve(".").relativize(response.getLocation()).toString();
    }


    @Override
    @After
    public void tearDown()
            throws Exception {
        super.tearDown();
    }


    @Test
    public void testIfPermissionIsSetOnceTheROisCreatedAndDeletedWithRO() {
        URI createdRO = createRO();
        Permission[] permissions = webResource.path("accesscontrol/permissions/")
                .queryParam("ro", createdRO.toString()).header("Authorization", "Bearer " + adminCreds)
                .accept(MediaType.APPLICATION_JSON).get(Permission[].class);

        Assert.assertEquals(permissions.length, 1);

        Permission handPermission = new Permission();
        handPermission.setRo(createdRO.toString());
        handPermission.setRole(Role.REDAER);
        handPermission.setUser(new UserProfile(userId, userId, pl.psnc.dl.wf4ever.dl.UserMetadata.Role.AUTHENTICATED));
        ClientResponse response = webResource.path("accesscontrol/permissions/").entity(handPermission)
                .type("application/json").accept("application/json").header("Authorization", "Bearer " + adminCreds)
                .post(ClientResponse.class);
        Permission serverPermission = response.getEntity(Permission.class);
        Assert.assertEquals(201, response.getStatus());
        Assert.assertEquals(permissions.length, 2);
        Assert.assertEquals(response.getLocation(), serverPermission.getUri());

        //conflict
        response = webResource.path("accesscontrol/permissions/").entity(handPermission)
                .type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminCreds).post(ClientResponse.class);
        Assert.assertEquals(409, response.getStatus());
        delete(createdRO, adminCreds);
        permissions = webResource.path("accesscontrol/permissions/").queryParam("ro", createdRO.toString())
                .header("Authorization", "Bearer " + adminCreds).accept(MediaType.APPLICATION_JSON)
                .get(Permission[].class);
        Assert.assertEquals(permissions.length, 0);
    }
}
