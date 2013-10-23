package pl.psnc.dl.wf4ever.accesscontrol;

import java.net.URI;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.accesscontrol.dicts.Mode;
import pl.psnc.dl.wf4ever.accesscontrol.model.AccessMode;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.sun.jersey.api.client.ClientResponse;

@Category(IntegrationTest.class)
public class ModesResourceTest extends AccessControlTest {

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
    public void testIfModeIsSetOnceTheROisCreatedAndDeletedWithRO() {
        URI createdRO = createRO();
        AccessMode mode = webResource.path("accesscontrol/modes/").queryParam("ro", createdRO.toString())
                .header("Authorization", "Bearer " + adminCreds).get(AccessMode.class);

        Assert.assertEquals(createdRO.toString(), mode.getRo());
        Assert.assertEquals(Mode.PUBLIC, mode.getMode());
        mode.setMode(Mode.PRIVATE);

        ClientResponse res = changeMode(mode.getUri(), mode.getMode());
        Assert.assertEquals(201, res.getStatus());

        mode = res.getEntity(AccessMode.class);
        Assert.assertEquals(Mode.PRIVATE, mode.getMode());

        AccessMode mode2 = webResource.uri(res.getLocation()).header("Authorization", "Bearer " + adminCreds)
                .accept("application/json").get(AccessMode.class);

        Assert.assertEquals(res.getLocation(), mode2.getUri());
        Assert.assertEquals(Mode.PRIVATE, mode2.getMode());

        delete(createdRO, adminCreds);
        ClientResponse response = webResource.path("accesscontrol/modes/").queryParam("ro", createdRO.toString())
                .header("Authorization", "Bearer " + adminCreds).get(ClientResponse.class);
        //no content :)
        Assert.assertEquals(response.getStatus(), 204);
    }

}
