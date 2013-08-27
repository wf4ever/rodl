package pl.psnc.dl.wf4ever.accesscontrol;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.accesscontrol.model.Mode;
import pl.psnc.dl.wf4ever.integration.AbstractIntegrationTest;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;

@Category(IntegrationTest.class)
public class ModeResourceTest extends AbstractIntegrationTest {

    @Override
    @Before
    public void setUp()
            throws Exception {
        super.setUp();
    }


    @Override
    @Before
    public void tearDown()
            throws Exception {
        super.tearDown();
    }


    @Test
    public void testIfModeIsSetOnceTheROisCreated() {

    }


    @Test
    public void testIfModeIsDeletefOnceTheROisDeleted() {

    }


    @Test
    public void testSinglePost() {
        Builder builder = webResource.path("evo/copy/").header("Authorization", "Bearer " + accessToken);
        Mode mode = new Mode();
        ClientResponse response = builder.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, mode);
    }
}
