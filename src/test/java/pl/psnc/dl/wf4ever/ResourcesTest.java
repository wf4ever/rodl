/**
 * 
 */
package pl.psnc.dl.wf4ever;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
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

	private final String clientRedirectionURI = "OOB"; //will not be used

	private String clientId;

	private final String adminCreds = StringUtils.trim(Base64
			.encodeBase64String("wfadmin:wfadmin!!!".getBytes()));

	private final String userId = UUID.randomUUID().toString();

	private final String userIdUrlSafe = StringUtils.trim(Base64
			.encodeBase64URLSafeString(userId.getBytes()));

	private WebResource webResource;

	private String accessToken;

	private final String w = "w";

	private final String r = "r";

	private final String v = "v1";

	private final String filePath = "foo/bar.txt";

	private URI annotationBodyURI;

	private final String username = "John Doe";


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


	/* (non-Javadoc)
	 * @see com.sun.jersey.test.framework.JerseyTest#setUp()
	 */
	@Override
	@Before
	public void setUp()
		throws Exception
	{
		super.setUp();
	}


	/* (non-Javadoc)
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
		webResource = resource();
		createClient();
		try {
			getClientsList();
			getClient();
			createUser();
			try {
				createAccessToken();
				try {
					getAccessTokensList();
					createWorkspace();
					try {
						getWorkspacesList();
						createRO();
						createVersion();
						getInitialManifest();
						getInitialAnnotations();
						addFile();
						getManifest();
						addAnnotation();
						getAnnotationBody();
						getAnnotations();
						deleteFile();
						getInitialManifest();
						//						getInitialAnnotations();
						deleteVersion();
						deleteRO();
					}
					finally {
						deleteWorkspace();
					}
				}
				finally {
					deleteAccessToken();
				}
			}
			finally {
				deleteUser();
			}
		}
		finally {
			deleteClient();
		}
	}


	private void createClient()
	{
		ClientResponse response = webResource
				.path("clients")
				.header("Authorization", "Bearer " + adminCreds)
				.post(ClientResponse.class,
					clientName + "\r\n" + clientRedirectionURI);
		assertEquals(201, response.getStatus());
		URI clientURI = response.getLocation();
		clientId = clientURI.resolve(".").relativize(clientURI).toString();
	}


	private void getClientsList()
	{
		String list = webResource.path("clients")
				.header("Authorization", "Bearer " + adminCreds)
				.get(String.class);
		assertTrue(list.contains(clientId));
	}


	private void getClient()
	{
		String client = webResource.path("clients/" + clientId)
				.header("Authorization", "Bearer " + adminCreds)
				.get(String.class);
		assertTrue(client.contains(clientId));
	}


	private void createUser()
	{
		ClientResponse response = webResource.path("users/" + userIdUrlSafe)
				.header("Authorization", "Bearer " + adminCreds)
				.put(ClientResponse.class, username);
		assertEquals(200, response.getStatus());
	}


	private void createAccessToken()
	{
		ClientResponse response = webResource.path("accesstokens")
				.header("Authorization", "Bearer " + adminCreds)
				.post(ClientResponse.class, clientId + "\r\n" + userId);
		assertEquals(201, response.getStatus());
		URI accessTokenURI = response.getLocation();
		accessToken = accessTokenURI.resolve(".").relativize(accessTokenURI)
				.toString();
	}


	private void getAccessTokensList()
	{
		String list = webResource.path("accesstokens")
				.header("Authorization", "Bearer " + adminCreds)
				.get(String.class);
		assertTrue(list.contains(accessToken));
	}


	private void createWorkspace()
	{
		ClientResponse response = webResource.path("workspaces")
				.header("Authorization", "Bearer " + accessToken)
				.post(ClientResponse.class, w);
		assertEquals(201, response.getStatus());
	}


	private void getWorkspacesList()
	{
		String list = webResource.path("workspaces")
				.header("Authorization", "Bearer " + accessToken)
				.get(String.class);
		assertTrue(list.contains(w));
	}


	private void createRO()
	{
		ClientResponse response = webResource.path("workspaces/" + w + "/ROs")
				.header("Authorization", "Bearer " + accessToken)
				.post(ClientResponse.class, r);
		assertEquals(201, response.getStatus());
	}


	private void createVersion()
	{
		ClientResponse response = webResource
				.path("workspaces/" + w + "/ROs/" + r)
				.header("Authorization", "Bearer " + accessToken)
				.post(ClientResponse.class, v);
		assertEquals(201, response.getStatus());
	}


	private void addFile()
	{
		ClientResponse response = webResource
				.path(
					"workspaces/" + w + "/ROs/" + r + "/" + v + "/" + filePath)
				.header("Authorization", "Bearer " + accessToken)
				.header("Content-Type", "text/plain")
				.put(ClientResponse.class, "lorem ipsum");
		assertEquals(200, response.getStatus());
	}


	private void getManifest()
	{
		String manifest = webResource
				.path(
					"workspaces/" + w + "/ROs/" + r + "/" + v
							+ "/.ro_metadata/manifest")
				.header("Authorization", "Bearer " + accessToken)
				.get(String.class);
		assertTrue(manifest.contains(username));
		assertTrue(manifest.contains(filePath));

		manifest = webResource
				.path(
					"workspaces/" + w + "/ROs/" + r + "/" + v
							+ "/.ro_metadata/manifest")
				.header("Authorization", "Bearer " + accessToken)
				.header("Content-Type", "text/turtle").get(String.class);
		assertTrue(manifest.contains(username));
		assertTrue(manifest.contains(filePath));
	}


	private void addAnnotation()
	{
		String resourceURI = getBaseURI().toString() + "workspaces/" + w
				+ "/ROs/" + r + "/" + v + "/" + filePath;
		String dcTitle = "http://dublincore.org/documents/dcmi-terms/title";
		ClientResponse response = webResource
				.path(
					"workspaces/" + w + "/ROs/" + r + "/" + v
							+ "/.ro_metadata/annotations")
				.header("Authorization", "Bearer " + accessToken)
				.header("Content-Type", "text/plain")
				.post(
					ClientResponse.class,
					String.format("%s %s \"%s\"", resourceURI, dcTitle,
						"My title"));
		assertEquals(201, response.getStatus());
		annotationBodyURI = response.getLocation();
	}


	private void getAnnotationBody()
	{
		String annotationBodyPath = getBaseURI().relativize(annotationBodyURI)
				.toString();
		String body = webResource.path(annotationBodyPath)
				.header("Authorization", "Bearer " + accessToken)
				.get(String.class);
		assertTrue("Annotation body should contain file path: " + filePath,
			body.contains(filePath));
		assertTrue(body.contains("My title"));
	}


	private void getAnnotations()
	{
		String annotations = webResource
				.path(
					"workspaces/" + w + "/ROs/" + r + "/" + v
							+ "/.ro_metadata/annotations")
				.header("Authorization", "Bearer " + accessToken)
				.get(String.class);
		assertTrue(annotations.contains(annotationBodyURI.toString()));

		annotations = webResource
				.path(
					"workspaces/" + w + "/ROs/" + r + "/" + v
							+ "/.ro_metadata/annotations")
				.header("Authorization", "Bearer " + accessToken)
				.header("Content-Type", "text/plain")

				.get(String.class);
		assertTrue(annotations.contains(annotationBodyURI.toString()));
	}


	private void deleteFile()
	{
		ClientResponse response = webResource
				.path(
					"workspaces/" + w + "/ROs/" + r + "/" + v + "/" + filePath)
				.header("Authorization", "Bearer " + accessToken)
				.header("Content-Type", "text/plain")
				.delete(ClientResponse.class);
		assertEquals(204, response.getStatus());
	}


	private void getInitialManifest()
	{
		String manifest = webResource
				.path(
					"workspaces/" + w + "/ROs/" + r + "/" + v
							+ "/.ro_metadata/manifest")
				.header("Authorization", "Bearer " + accessToken)
				.get(String.class);
		assertTrue(manifest.contains(username));
		assertTrue(!manifest.contains(filePath));

		manifest = webResource
				.path(
					"workspaces/" + w + "/ROs/" + r + "/" + v
							+ "/.ro_metadata/manifest")
				.header("Authorization", "Bearer " + accessToken)
				.header("Content-Type", "text/turtle").get(String.class);
		assertTrue(manifest.contains(username));
		assertTrue(!manifest.contains(filePath));
	}


	private void getInitialAnnotations()
	{
		String annotations = webResource
				.path(
					"workspaces/" + w + "/ROs/" + r + "/" + v
							+ "/.ro_metadata/annotations")
				.header("Authorization", "Bearer " + accessToken)
				.get(String.class);
		assertNotNull("Annotations cannot be null", annotations);
		Assert.assertNotSame("Annotations cannot be empty", "", annotations);
		if (annotationBodyURI != null)
			assertTrue(!annotations.contains(annotationBodyURI.toString()));

		annotations = webResource
				.path(
					"workspaces/" + w + "/ROs/" + r + "/" + v
							+ "/.ro_metadata/annotations")
				.header("Authorization", "Bearer " + accessToken)
				.header("Content-Type", "text/plain").get(String.class);
		assertNotNull("Annotations cannot be null", annotations);
		Assert.assertNotSame("Annotations cannot be empty", "", annotations);
		if (annotationBodyURI != null)
			assertTrue(!annotations.contains(annotationBodyURI.toString()));
	}


	private void deleteVersion()
	{
		ClientResponse response = webResource
				.path("workspaces/" + w + "/ROs/" + r + "/" + v)
				.header("Authorization", "Bearer " + accessToken)
				.delete(ClientResponse.class);
		assertEquals(204, response.getStatus());
	}


	private void deleteRO()
	{
		ClientResponse response = webResource
				.path("workspaces/" + w + "/ROs/" + r)
				.header("Authorization", "Bearer " + accessToken)
				.delete(ClientResponse.class);
		assertEquals(204, response.getStatus());
	}


	private void deleteWorkspace()
	{
		ClientResponse response = webResource.path("workspaces/" + w)
				.header("Authorization", "Bearer " + accessToken)
				.delete(ClientResponse.class);
		assertEquals(204, response.getStatus());
	}


	private void deleteAccessToken()
	{
		ClientResponse response = webResource
				.path("accesstokens/" + accessToken)
				.header("Authorization", "Bearer " + adminCreds)
				.delete(ClientResponse.class);
		assertEquals(204, response.getStatus());
	}


	private void deleteUser()
	{
		ClientResponse response = webResource.path("users/" + userIdUrlSafe)
				.header("Authorization", "Bearer " + adminCreds)
				.delete(ClientResponse.class);
		assertEquals(204, response.getStatus());
	}


	private void deleteClient()
	{
		ClientResponse response = webResource.path("clients/" + clientId)
				.header("Authorization", "Bearer " + adminCreds)
				.delete(ClientResponse.class);
		assertEquals(204, response.getStatus());
	}
}
