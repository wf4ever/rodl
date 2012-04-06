/**
 * 
 */
package pl.psnc.dl.wf4ever;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.external.ExternalTestContainerFactory;

/**
 * @author piotrhol
 * 
 */
public class ResourcesTest
	extends JerseyTest
{

	private final String clientName = "ROSRS testing app written in Ruby";

	private final String clientRedirectionURI = "OOB"; // will not be used

	private String clientId;

	private final String adminCreds = StringUtils.trim(Base64.encodeBase64String("wfadmin:wfadmin!!!".getBytes()));

	private final String userId = UUID.randomUUID().toString();

	private final String userIdUrlSafe = StringUtils.trim(Base64.encodeBase64URLSafeString(userId.getBytes()));

	private final String userId2 = UUID.randomUUID().toString();

	private final String userId2UrlSafe = StringUtils.trim(Base64.encodeBase64URLSafeString(userId2.getBytes()));

	private WebResource webResource;

	private String accessToken;

	private String accessToken2;

	private final String r = "r";

	private final String r2 = "r2";

	private final String filePath = "foo/bar ra.txt";

	private final String filePathEncoded = "foo/bar%20ra.txt";

	private final String rdfFilePath = "foo/bar.rdf";

	private final String rdfFilePathEncoded = "foo/bar.rdf";

	private final String annotationBodyURI = ".ro/ann1.ttl";

	private final String username = "John Doe";

	private final String username2 = "May Gray";


	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass()
		throws Exception
	{
	}


	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass()
		throws Exception
	{
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.jersey.test.framework.JerseyTest#setUp()
	 */
	@Override
	@Before
	public void setUp()
		throws Exception
	{
		super.setUp();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.jersey.test.framework.JerseyTest#tearDown()
	 */
	@Override
	@After
	public void tearDown()
		throws Exception
	{
		super.tearDown();
	}


	public ResourcesTest()
	{
		super("pl.psnc.dl.wf4ever");
	}


	@Override
	protected TestContainerFactory getTestContainerFactory()
		throws TestContainerException
	{
		return new ExternalTestContainerFactory();
	}


	@Test
	public final void test()
	{
		client().setFollowRedirects(true);
		if (resource().getURI().getHost().equals("localhost")) {
			webResource = resource();
		}
		else {
			webResource = resource().path("rosrs5/");
		}
		createClient();
		try {
			getClientsList();
			getClient();
			createUsers();
			getUser();
			try {
				createAccessTokens();
				try {
					getAccessTokensList();
					checkWhoAmI();
					webResource.path("ROs/" + r + "/").header("Authorization", "Bearer " + accessToken)
							.delete(ClientResponse.class);
					webResource.path("ROs/" + r2 + "/").header("Authorization", "Bearer " + accessToken)
							.delete(ClientResponse.class);
					createROs();
					try {
						getROsList();
						getInitialManifest();
						updateManifest();
						addFile();
						getFileMetadata();
						getFileContent();
						addRDFFile();
						getRDFFileMetadata();
						getRDFFileContent();
						getManifest();
						addAnnotationBody();
						getAnnotationBody();
						getManifestWithAnnotationBody();
						deleteFile();
						deleteRDFFile();
						getInitialManifest();
					}
					finally {
						deleteROs();
					}
				}
				finally {
					deleteAccessTokens();
				}
			}
			finally {
				deleteUsers();
			}
		}
		finally {
			deleteClient();
		}
	}


	private void getUser()
	{
		String user = webResource.path("users/" + userIdUrlSafe).header("Authorization", "Bearer " + adminCreds)
				.get(String.class);
		assertTrue(user.contains(userId));

		user = webResource.path("users/" + userId2UrlSafe).header("Authorization", "Bearer " + adminCreds)
				.get(String.class);
		assertTrue(user.contains(userId2));

		user = webResource.path("users/" + userId2UrlSafe).header("Authorization", "Bearer " + adminCreds)
				.accept("application/x-turtle").get(String.class);
		assertTrue(user.contains(userId2));
	}


	private void createClient()
	{
		ClientResponse response = webResource.path("clients/").header("Authorization", "Bearer " + adminCreds)
				.post(ClientResponse.class, clientName + "\r\n" + clientRedirectionURI);
		assertEquals(201, response.getStatus());
		URI clientURI = response.getLocation();
		clientId = clientURI.resolve(".").relativize(clientURI).toString();
	}


	private void getClientsList()
	{
		String list = webResource.path("clients/").header("Authorization", "Bearer " + adminCreds).get(String.class);
		assertTrue(list.contains(clientId));
	}


	private void getClient()
	{
		String client = webResource.path("clients/" + clientId).header("Authorization", "Bearer " + adminCreds)
				.get(String.class);
		assertTrue(client.contains(clientId));
	}


	private void createUsers()
	{
		ClientResponse response = webResource.path("users/" + userIdUrlSafe)
				.header("Authorization", "Bearer " + adminCreds).put(ClientResponse.class, username);
		assertEquals(200, response.getStatus());

		response = webResource.path("users/" + userId2UrlSafe).header("Authorization", "Bearer " + adminCreds)
				.put(ClientResponse.class, username2);
		assertEquals(200, response.getStatus());
	}


	private void createAccessTokens()
	{
		ClientResponse response = webResource.path("accesstokens/").header("Authorization", "Bearer " + adminCreds)
				.post(ClientResponse.class, clientId + "\r\n" + userId);
		assertEquals(201, response.getStatus());
		URI accessTokenURI = response.getLocation();
		accessToken = accessTokenURI.resolve(".").relativize(accessTokenURI).toString();

		response = webResource.path("accesstokens/").header("Authorization", "Bearer " + adminCreds)
				.post(ClientResponse.class, clientId + "\r\n" + userId2);
		assertEquals(201, response.getStatus());
		accessTokenURI = response.getLocation();
		accessToken2 = accessTokenURI.resolve(".").relativize(accessTokenURI).toString();
	}


	private void getAccessTokensList()
	{
		String list = webResource.path("accesstokens/").header("Authorization", "Bearer " + adminCreds)
				.get(String.class);
		assertTrue(list.contains(accessToken));
	}


	private void checkWhoAmI()
	{
		String whoami = webResource.path("whoami/").header("Authorization", "Bearer " + accessToken).get(String.class);
		assertTrue(whoami.contains(userId));
		assertTrue(whoami.contains(username));

		whoami = webResource.path("whoami/").header("Authorization", "Bearer " + accessToken2).get(String.class);
		assertTrue(whoami.contains(userId2));
		assertTrue(whoami.contains(username2));

		try {
			webResource.path("whoami/").get(Response.class);
			fail("WhoAmI requests without access tokens should throw 401 Unauthorized");
		}
		catch (UniformInterfaceException e) {
			assertEquals("WhoAmI requests without access tokens should throw 401 Unauthorized", 401, e.getResponse()
					.getStatus());

		}
	}


	private void createROs()
	{
		ClientResponse response = webResource.path("ROs/").header("Authorization", "Bearer " + accessToken)
				.post(ClientResponse.class, r);
		assertEquals(201, response.getStatus());

		response = webResource.path("ROs/").header("Authorization", "Bearer " + accessToken2)
				.post(ClientResponse.class, r2);
		assertEquals(201, response.getStatus());
	}


	private void getROsList()
	{
		String list = webResource.path("ROs").header("Authorization", "Bearer " + accessToken).get(String.class);
		assertTrue(list.contains(r));
		assertTrue(!list.contains(r2));

		list = webResource.path("ROs").get(String.class);
		assertTrue(list.contains(r));
		assertTrue(list.contains(r2));
	}


	private void updateManifest()
	{
		InputStream is = getClass().getClassLoader().getResourceAsStream("manifest.ttl");
		ClientResponse response = webResource.path("ROs/" + r + "/.ro/manifest.rdf")
				.header("Authorization", "Bearer " + accessToken).type("text/turtle").put(ClientResponse.class, is);
		assertEquals(200, response.getStatus());
	}


	private void addFile()
	{
		ClientResponse response = webResource.path("ROs/" + r + "/" + filePath)
				.header("Authorization", "Bearer " + accessToken).type("text/plain")
				.put(ClientResponse.class, "lorem ipsum");
		assertEquals(200, response.getStatus());
	}


	private void getFileMetadata()
	{
		String metadata = webResource.path("ROs/" + r + "/" + filePath)
				.header("Authorization", "Bearer " + accessToken).get(String.class);
		assertTrue(metadata.contains(userId));
		assertTrue(metadata.contains(filePathEncoded));
		assertTrue(metadata.contains("checksum"));
	}


	private void getFileContent()
	{
		String metadata = webResource.path("ROs/" + r + "/" + filePath).queryParam("content", "true")
				.header("Authorization", "Bearer " + accessToken).get(String.class);
		assertTrue(metadata.contains("lorem ipsum"));

	}


	private void addRDFFile()
	{
		ClientResponse response = webResource.path("ROs/" + r + "/" + rdfFilePath)
				.header("Authorization", "Bearer " + accessToken).type(RDFFormat.RDFXML.getDefaultMIMEType())
				.put(ClientResponse.class, "lorem ipsum");
		assertEquals(200, response.getStatus());
	}


	private void getRDFFileMetadata()
	{
		String metadata = webResource.path("ROs/" + r + "/" + rdfFilePath)
				.header("Authorization", "Bearer " + accessToken).get(String.class);
		assertTrue(metadata.contains(userId));
		assertTrue(metadata.contains(rdfFilePathEncoded));
		assertTrue(metadata.contains("checksum"));
	}


	private void getRDFFileContent()
	{
		String metadata = webResource.path("ROs/" + r + "/" + rdfFilePath).queryParam("content", "true")
				.header("Authorization", "Bearer " + accessToken).get(String.class);
		assertTrue(metadata.contains("lorem ipsum"));

	}


	private void getManifest()
	{
		String manifest = webResource.path("/ROs/" + r + "/.ro/manifest.rdf")
				.header("Authorization", "Bearer " + accessToken).get(String.class);
		assertTrue(manifest.contains(userId));
		assertTrue(manifest.contains(filePathEncoded));

		manifest = webResource.path("/ROs/" + r + "/.ro/manifest.rdf").header("Authorization", "Bearer " + accessToken)
				.accept("application/x-turtle").get(String.class);
		assertTrue(manifest.contains(userId));
		assertTrue(manifest.contains(filePathEncoded));

		manifest = webResource.path("/ROs/" + r + "/.ro/manifest.n3").queryParam("original", "manifest.rdf")
				.header("Authorization", "Bearer " + accessToken).get(String.class);
		assertTrue(manifest.contains(userId));
		assertTrue(manifest.contains(filePathEncoded));

		ClientResponse response = webResource.path("/ROs/" + r + "/.ro/manifest.n3")
				.header("Authorization", "Bearer " + accessToken).get(ClientResponse.class);
		assertEquals("Should return 404 for manifest.n3", HttpStatus.SC_NOT_FOUND, response.getStatus());
	}


	private void getManifestWithAnnotationBody()
	{
		String manifest = webResource.path("/ROs/" + r + "/.ro/manifest.rdf")
				.header("Authorization", "Bearer " + accessToken).accept("application/x-trig").get(String.class);
		assertTrue(manifest.contains(userId));
		assertTrue(manifest.contains(filePathEncoded));
		assertTrue("Annotation body should contain file path: " + filePath, manifest.contains("a_workflow.t2flow"));
		assertTrue(manifest.contains("A test"));

		manifest = webResource.path("/ROs/" + r + "/.ro/manifest.trig").queryParam("original", "manifest.rdf")
				.header("Authorization", "Bearer " + accessToken).get(String.class);
		assertTrue(manifest.contains(userId));
		assertTrue(manifest.contains(filePathEncoded));
		assertTrue("Annotation body should contain file path: " + filePathEncoded,
			manifest.contains("a_workflow.t2flow"));
		assertTrue(manifest.contains("A test"));
	}


	private void addAnnotationBody()
	{
		InputStream is = getClass().getClassLoader().getResourceAsStream("annotationBody.ttl");
		ClientResponse response = webResource.path("ROs/" + r + "/" + annotationBodyURI)
				.header("Authorization", "Bearer " + accessToken).type("application/x-turtle")
				.put(ClientResponse.class, is);
		assertEquals(200, response.getStatus());
	}


	private void getAnnotationBody()
	{
		String body = webResource.path("ROs/" + r + "/" + annotationBodyURI)
				.header("Authorization", "Bearer " + accessToken).get(String.class);
		assertTrue("Annotation body should contain file path: a_workflow.t2flow", body.contains("a_workflow.t2flow"));
		assertTrue(body.contains("A test"));
	}


	private void deleteFile()
	{
		ClientResponse response = webResource.path("ROs/" + r + "/" + filePath)
				.header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
		assertEquals(204, response.getStatus());
	}


	private void deleteRDFFile()
	{
		ClientResponse response = webResource.path("ROs/" + r + "/" + rdfFilePath)
				.header("Authorization", "Bearer " + accessToken).type(RDFFormat.RDFXML.getDefaultMIMEType())
				.delete(ClientResponse.class);
		assertEquals(204, response.getStatus());
	}


	private void getInitialManifest()
	{
		String manifest = webResource.path("ROs/" + r + "/.ro/manifest.rdf")
				.header("Authorization", "Bearer " + accessToken).get(String.class);
		assertTrue(!manifest.contains(filePathEncoded));

		manifest = webResource.path("ROs/" + r + "/.ro/manifest.rdf").header("Authorization", "Bearer " + accessToken)
				.accept("application/x-turtle").get(String.class);
		assertTrue(!manifest.contains(filePathEncoded));
	}


	private void deleteROs()
	{
		ClientResponse response = webResource.path("ROs/" + r + "/").header("Authorization", "Bearer " + accessToken)
				.delete(ClientResponse.class);
		assertEquals(204, response.getStatus());

		response = webResource.path("ROs/" + r2 + "/").header("Authorization", "Bearer " + accessToken2)
				.delete(ClientResponse.class);
		assertEquals(204, response.getStatus());
	}


	private void deleteAccessTokens()
	{
		ClientResponse response = webResource.path("accesstokens/" + accessToken)
				.header("Authorization", "Bearer " + adminCreds).delete(ClientResponse.class);
		assertEquals(204, response.getStatus());

		response = webResource.path("accesstokens/" + accessToken2).header("Authorization", "Bearer " + adminCreds)
				.delete(ClientResponse.class);
		assertEquals(204, response.getStatus());
	}


	private void deleteUsers()
	{
		ClientResponse response = webResource.path("users/" + userIdUrlSafe)
				.header("Authorization", "Bearer " + adminCreds).delete(ClientResponse.class);
		assertEquals(204, response.getStatus());

		response = webResource.path("users/" + userId2UrlSafe).header("Authorization", "Bearer " + adminCreds)
				.delete(ClientResponse.class);
		assertEquals(204, response.getStatus());
	}


	private void deleteClient()
	{
		ClientResponse response = webResource.path("clients/" + clientId)
				.header("Authorization", "Bearer " + adminCreds).delete(ClientResponse.class);
		assertEquals(204, response.getStatus());
	}
}
