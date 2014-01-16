package pl.psnc.dl.wf4ever.accesscontrol;

import java.net.URI;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.accesscontrol.dicts.Role;
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
    public void testReaderWriterAndAuthorShoulBeAbleToReadRO() {
        URI createdRO = createRO(accessToken);
        ClientResponse response = webResource.uri(createdRO).header("Authorization", "Bearer " + accessToken)
                .get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus());
        grantPermission(createdRO, Role.READER, userProfile2);
        response = webResource.uri(createdRO).accept("application/json")
                .header("Authorization", "Bearer " + accessToken2).get(ClientResponse.class);
        grantPermission(createdRO, Role.READER, userProfile2);
        response = webResource.uri(createdRO).accept("application/json")
                .header("Authorization", "Bearer " + accessToken2).get(ClientResponse.class);
        response = webResource.uri(createdRO).accept("application/json")
                .header("Authorization", "Bearer " + accessToken2).get(ClientResponse.class);
    }

}
