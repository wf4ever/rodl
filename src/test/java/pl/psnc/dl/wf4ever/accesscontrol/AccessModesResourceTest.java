package pl.psnc.dl.wf4ever.accesscontrol;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.accesscontrol.model.AccessMode;
import pl.psnc.dl.wf4ever.integration.AbstractIntegrationTest;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;

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
        delete(createdRO, adminCreds);
        ClientResponse response = webResource.path("accesscontrol/modes/").queryParam("ro", createdRO.toString())
                .header("Authorization", "Bearer " + adminCreds).get(ClientResponse.class);
        //no content :)
        Assert.assertEquals(response.getStatus(), 204);
    }


    @Test
    public void testSinglePost() {
        Builder builder = webResource.path("accesscontrol/modes/").header("Authorization", "Bearer " + accessToken);
        AccessMode mode = new AccessMode();
        ClientResponse response = builder.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, mode);
    }
}
