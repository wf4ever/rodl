package pl.psnc.dl.wf4ever.accesscontrol.filters;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.accesscontrol.AccessControlTest;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.sun.jersey.api.client.ClientResponse;

@Category(IntegrationTest.class)
public class AccessControlAdminResourceTest extends AccessControlTest {

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
    public void testIfOnlyAdminCanSeeAdminPage() {
        ClientResponse response = webResource.path("admin/fakeUri").header("Authorization", "Bearer " + adminCreds)
                .get(ClientResponse.class);
        Assert.assertEquals(404, response.getClientResponseStatus().getStatusCode());
        response = webResource.path("admin/fakeUri").header("Authorization", "Bearer " + accessToken)
                .get(ClientResponse.class);
        Assert.assertEquals(403, response.getClientResponseStatus().getStatusCode());
    }

}
