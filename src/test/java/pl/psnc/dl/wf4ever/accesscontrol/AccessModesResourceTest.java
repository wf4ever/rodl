package pl.psnc.dl.wf4ever.accesscontrol;

import java.net.URI;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.accesscontrol.dicts.Mode;
import pl.psnc.dl.wf4ever.accesscontrol.model.AccessMode;
import pl.psnc.dl.wf4ever.integration.AbstractIntegrationTest;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.sun.jersey.api.client.ClientResponse;

@Category(IntegrationTest.class)
public class AccessModesResourceTest extends AbstractIntegrationTest {

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

        ClientResponse res = webResource.uri(mode.getUri().resolve("")).entity(mode).type("application/json")
                .accept("application/json").post(ClientResponse.class);

        mode = webResource.uri(res.getLocation()).header("Authorization", "Bearer " + adminCreds)
                .accept("application/json").get(AccessMode.class);
        Assert.assertEquals(res.getLocation(), mode.getUri());
        Assert.assertEquals(Mode.PRIVATE, mode.getMode());

        delete(createdRO, adminCreds);
        ClientResponse response = webResource.path("accesscontrol/modes/").queryParam("ro", createdRO.toString())
                .header("Authorization", "Bearer " + adminCreds).get(ClientResponse.class);
        //no content :)
        Assert.assertEquals(response.getStatus(), 204);
    }
}
