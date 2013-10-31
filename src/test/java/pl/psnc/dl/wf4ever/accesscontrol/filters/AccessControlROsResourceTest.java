package pl.psnc.dl.wf4ever.accesscontrol.filters;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.accesscontrol.AccessControlTest;
import pl.psnc.dl.wf4ever.accesscontrol.dicts.Role;
import pl.psnc.dl.wf4ever.accesscontrol.model.Permission;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.sun.jersey.api.client.ClientResponse;

@Category(IntegrationTest.class)
public class AccessControlROsResourceTest extends AccessControlTest {

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
    public void testIfOnlyOnwerCanReadAndWriteModes() {
        URI createdRO = createRO(accessToken);
    }

}
