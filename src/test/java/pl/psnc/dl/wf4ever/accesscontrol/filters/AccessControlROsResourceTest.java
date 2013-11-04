package pl.psnc.dl.wf4ever.accesscontrol.filters;

import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.accesscontrol.AccessControlTest;
import pl.psnc.dl.wf4ever.accesscontrol.dicts.Role;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.sun.jersey.api.client.ClientResponse;

@Category(IntegrationTest.class)
public class AccessControlROsResourceTest extends AccessControlTest {

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testIfEverybodyCanReadButNobodyCanWrite() {
		URI createdRO = createRO(accessToken);
		// get
		ClientResponse response = webResource.uri(createdRO)
				.header("Authorization", "Bearer " + accessToken2)
				.get(ClientResponse.class);
		Assert.assertEquals(200, response.getStatus());
		response = webResource.uri(createdRO).path(".ro/manifest.rdf")
				.header("Authorization", "Bearer " + accessToken2)
				.get(ClientResponse.class);
		Assert.assertEquals(200, response.getStatus());
		// put
		response = addFile(createdRO, "file_path",
				IOUtils.toInputStream("content"), "text/plain", accessToken2);
		Assert.assertEquals(403, response.getStatus());
		response = addFile(createdRO, "file_path",
				IOUtils.toInputStream("content"), "text/plain", accessToken);
		Assert.assertEquals(201, response.getStatus());
		URI resourceLocation = response.getLocation();
		// delete
		response = delete(resourceLocation, accessToken2);
		Assert.assertEquals(403, response.getStatus());
		response = delete(resourceLocation, accessToken);
		Assert.assertEquals(204, response.getStatus());

		response = delete(createdRO, accessToken2);
		Assert.assertEquals(403, response.getStatus());
		response = delete(createdRO, accessToken);
		Assert.assertEquals(204, response.getStatus());
	}

	public void testIfSomeoneWithWritterPermissionCanEdit() {
		URI createdRO = createRO(accessToken);
		grantPermission(createdRO, Role.EDITOR, userProfile2);
		// should be able to edti
		ClientResponse response = addFile(createdRO, "file_path",
				IOUtils.toInputStream("content"), "text/plain", accessToken2);
		Assert.assertEquals(201, response.getStatus());
		URI resourceLocation = response.getLocation();
		// should be able to delete resource
		response = delete(resourceLocation, accessToken2);
		Assert.assertEquals(204, response.getStatus());
		// but shouldn't be able to delete the whole RO
		response = delete(createdRO, accessToken2);
		Assert.assertEquals(403, response.getStatus());

	}
}
