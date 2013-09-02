package pl.psnc.dl.wf4ever.accesscontrol;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.accesscontrol.model.Permission;
import pl.psnc.dl.wf4ever.integration.AbstractIntegrationTest;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

@Category(IntegrationTest.class)
public class PermissionResourceTest extends AbstractIntegrationTest {

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
    public void testIfPermissionIsSetOnceTheROisCreatedAndDeletedWithRO() {
        URI createdRO = createRO();
        Permission[] permissions = webResource.path("accesscontrol/permissions/")
                .queryParam("ro", createdRO.toString()).header("Authorization", "Bearer " + adminCreds)
                .accept(MediaType.APPLICATION_JSON).get(Permission[].class);
        delete(createdRO, adminCreds);
        permissions = webResource.path("accesscontrol/permissions/").queryParam("ro", createdRO.toString())
                .header("Authorization", "Bearer " + adminCreds).accept(MediaType.APPLICATION_JSON)
                .get(Permission[].class);
        Assert.assertEquals(permissions.length, 0);
    }
}
