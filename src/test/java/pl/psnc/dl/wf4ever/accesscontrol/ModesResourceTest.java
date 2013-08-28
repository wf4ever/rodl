package pl.psnc.dl.wf4ever.accesscontrol;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.accesscontrol.model.Mode;
import pl.psnc.dl.wf4ever.integration.AbstractIntegrationTest;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;

@Category(IntegrationTest.class)
public class ModesResourceTest extends AbstractIntegrationTest {

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
    public void testIfModeIsSetOnceTheROisCreated() {
        URI createdRO = createRO();
        Mode mode = webResource.path("accesscontrol/modes/").queryParam("ro", createdRO.toString())
                .header("Authorization", "Bearer " + adminCreds).get(Mode.class);

        System.out.println("*******************");
        System.out.println("*******************");
        System.out.println("*******************");
        System.out.println("*******************");
        System.out.println(mode.getMode());
        System.out.println(mode.getId());
        System.out.println(mode.getRo());
        System.out.println("*******************");
        System.out.println("*******************");
        System.out.println("*******************");
        System.out.println("*******************");
        delete(createdRO, adminCreds);

    }


    @Test
    public void testIfModeIsDeletefOnceTheROisDeleted() {

    }


    @Test
    public void testSinglePost() {
        Builder builder = webResource.path("accesscontrol/modes/").header("Authorization", "Bearer " + accessToken);
        Mode mode = new Mode();
        ClientResponse response = builder.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, mode);
    }
}
