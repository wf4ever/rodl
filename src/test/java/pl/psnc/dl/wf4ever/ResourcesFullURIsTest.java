/**
 * 
 */
package pl.psnc.dl.wf4ever;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

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
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.external.ExternalTestContainerFactory;

/**
 * @author piotrhol
 * 
 */
public class ResourcesFullURIsTest
	extends JerseyTest
{

	private final String clientName = "ROSRS testing app written in Ruby";

	private final String clientRedirectionURI = "OOB"; // will not be used

	private String clientId;

	private final String adminCreds = StringUtils.trim(Base64.encodeBase64String("wfadmin:wfadmin!!!".getBytes()));

	private final String userId = UUID.randomUUID().toString();

	private final String userIdUrlSafe = StringUtils.trim(Base64.encodeBase64URLSafeString(userId.getBytes()));

	private WebResource webResource;

	private String accessToken;

	private final String w = "w";

	private final String r = "r";

	private final String v = "v1";

	private final String filePath = "foo/bar ra.txt";

	private final String filePathEncoded = "foo/bar%20ra.txt";

	private final String rdfFilePath = "foo/bar.rdf";

	private final String rdfFilePathEncoded = "foo/bar.rdf";

	private final String annotationBodyURI = ".ro/ann1.ttl";

	private final String annotationBodyURIRDF = ".ro/ann1.rdf";

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


	public ResourcesFullURIsTest()
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
	public final void testFullURIs()
	{
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
						deleteAnnotationBody();
						deleteFile();
						deleteRDFFile();
						getInitialManifest();
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


	private void updateManifest()
	{
		InputStream is = getClass().getClassLoader().getResourceAsStream("manifest.ttl");
		ClientResponse response = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/.ro/manifest.rdf")
				.header("Authorization", "Bearer " + accessToken).type("application/x-turtle")
				.put(ClientResponse.class, is);
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
	}


	private void createClient()
	{
		ClientResponse response = webResource.path("clients/").header("Authorization", "Bearer " + adminCreds)
				.post(ClientResponse.class, clientName + "\r\n" + clientRedirectionURI);
		assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
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


	private void createUser()
	{
		ClientResponse response = webResource.path("users/" + userIdUrlSafe)
				.header("Authorization", "Bearer " + adminCreds).put(ClientResponse.class, username);
		assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
	}


	private void createAccessToken()
	{
		ClientResponse response = webResource.path("accesstokens/").header("Authorization", "Bearer " + adminCreds)
				.post(ClientResponse.class, clientId + "\r\n" + userId);
		assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
		URI accessTokenURI = response.getLocation();
		accessToken = accessTokenURI.resolve(".").relativize(accessTokenURI).toString();
	}


	private void getAccessTokensList()
	{
		String list = webResource.path("accesstokens/").header("Authorization", "Bearer " + adminCreds)
				.get(String.class);
		assertTrue(list.contains(accessToken));
	}


	private void createWorkspace()
	{
		ClientResponse response = webResource.path("workspaces/").header("Authorization", "Bearer " + accessToken)
				.post(ClientResponse.class, w);
		assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
	}


	private void getWorkspacesList()
	{
		String list = webResource.path("workspaces/").header("Authorization", "Bearer " + accessToken)
				.get(String.class);
		assertTrue(list.contains(w));
	}


	private void createRO()
	{
		ClientResponse response = webResource.path("workspaces/" + w + "/ROs/")
				.header("Authorization", "Bearer " + accessToken).post(ClientResponse.class, r);
		assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
	}


	private void createVersion()
	{
		ClientResponse response = webResource.path("workspaces/" + w + "/ROs/" + r)
				.header("Authorization", "Bearer " + accessToken).post(ClientResponse.class, v);
		assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
	}


	private void addFile()
	{
		ClientResponse response = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/" + filePath)
				.header("Authorization", "Bearer " + accessToken).type("text/plain")
				.put(ClientResponse.class, "lorem ipsum");
		assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
	}


	private void getFileMetadata()
	{
		String metadata = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/" + filePath)
				.header("Authorization", "Bearer " + accessToken).get(String.class);
		assertTrue(metadata.contains(userId));
		assertTrue(metadata.contains(filePathEncoded));
		assertTrue(metadata.contains("checksum"));

	}


	private void getFileContent()
	{
		String metadata = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/" + filePath)
				.queryParam("content", "true").header("Authorization", "Bearer " + accessToken).get(String.class);
		assertTrue(metadata.contains("lorem ipsum"));

	}


	private void addRDFFile()
	{
		ClientResponse response = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/" + rdfFilePath)
				.header("Authorization", "Bearer " + accessToken).type(RDFFormat.RDFXML.getDefaultMIMEType())
				.put(ClientResponse.class, "lorem ipsum");
		assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
	}


	private void getRDFFileMetadata()
	{
		String metadata = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/" + rdfFilePath)
				.header("Authorization", "Bearer " + accessToken).get(String.class);
		System.out.println(metadata);
		assertTrue(metadata.contains(userId));
		assertTrue(metadata.contains(rdfFilePathEncoded));
		assertTrue(metadata.contains("checksum"));
	}


	private void getRDFFileContent()
	{
		String metadata = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/" + rdfFilePath)
				.queryParam("content", "true").header("Authorization", "Bearer " + accessToken).get(String.class);
		assertTrue(metadata.contains("lorem ipsum"));

	}


	private void getManifest()
	{
		String manifest = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/.ro/manifest.rdf")
				.header("Authorization", "Bearer " + accessToken).get(String.class);
		assertTrue(manifest.contains(userId));
		assertTrue(manifest.contains(filePathEncoded));

		manifest = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/.ro/manifest.rdf")
				.header("Authorization", "Bearer " + accessToken).accept("application/x-turtle").get(String.class);
		assertTrue(manifest.contains(userId));
		assertTrue(manifest.contains(filePathEncoded));

		manifest = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/.ro/manifest.n3")
				.queryParam("original", "manifest.rdf").header("Authorization", "Bearer " + accessToken)
				.get(String.class);
		assertTrue(manifest.contains(userId));
		assertTrue(manifest.contains(filePathEncoded));

		ClientResponse response = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/.ro/manifest.n3")
				.header("Authorization", "Bearer " + accessToken).get(ClientResponse.class);
		assertEquals("Should return 404 for manifest.n3", HttpStatus.SC_NOT_FOUND, response.getStatus());
	}


	private void getManifestWithAnnotationBody()
	{
		String manifest = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/.ro/manifest.rdf")
				.header("Authorization", "Bearer " + accessToken).accept("application/x-trig").get(String.class);
		assertTrue(manifest.contains(userId));
		assertTrue(manifest.contains(filePathEncoded));
		assertTrue("Annotation body should contain file path: " + filePath, manifest.contains("a_workflow.t2flow"));
		assertTrue(manifest.contains("A test"));

		manifest = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/.ro/manifest.trig")
				.queryParam("original", "manifest.rdf").header("Authorization", "Bearer " + accessToken)
				.get(String.class);
		assertTrue(manifest.contains(userId));
		assertTrue(manifest.contains(filePathEncoded));
		assertTrue("Annotation body should contain file path: " + filePathEncoded,
			manifest.contains("a_workflow.t2flow"));
		assertTrue(manifest.contains("A test"));
	}


	private void addAnnotationBody()
	{
		InputStream is = getClass().getClassLoader().getResourceAsStream("annotationBody.ttl");
		ClientResponse response = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/" + annotationBodyURI)
				.header("Authorization", "Bearer " + accessToken).type("application/x-turtle")
				.put(ClientResponse.class, is);
		assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
	}


	private void getAnnotationBody()
	{
		String body = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/" + annotationBodyURI)
				.header("Authorization", "Bearer " + accessToken).get(String.class);
		assertTrue("Annotation body should contain file path: a_workflow.t2flow", body.contains("a_workflow.t2flow"));
		assertTrue(body.contains("A test"));
	}


	private void deleteAnnotationBody()
	{
		ClientResponse response = webResource
				.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/" + annotationBodyURIRDF)
				.queryParam("original", "ann1.ttl").header("Authorization", "Bearer " + accessToken)
				.delete(ClientResponse.class);
		assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
	}


	private void deleteFile()
	{
		ClientResponse response = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/" + filePath)
				.header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
		assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
	}


	private void deleteRDFFile()
	{
		ClientResponse response = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/" + rdfFilePath)
				.header("Authorization", "Bearer " + accessToken).type(RDFFormat.RDFXML.getDefaultMIMEType())
				.delete(ClientResponse.class);
		assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
	}


	private void getInitialManifest()
	{
		String manifest = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/.ro/manifest.rdf")
				.header("Authorization", "Bearer " + accessToken).get(String.class);
		assertTrue(!manifest.contains(filePathEncoded));

		manifest = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v + "/.ro/manifest.rdf")
				.header("Authorization", "Bearer " + accessToken).accept("text/turtle").get(String.class);
		assertTrue(!manifest.contains(filePathEncoded));
	}


	private void deleteVersion()
	{
		ClientResponse response = webResource.path("workspaces/" + w + "/ROs/" + r + "/" + v)
				.header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
		assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
	}


	private void deleteRO()
	{
		ClientResponse response = webResource.path("workspaces/" + w + "/ROs/" + r)
				.header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
		assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
	}


	private void deleteWorkspace()
	{
		ClientResponse response = webResource.path("workspaces/" + w).header("Authorization", "Bearer " + accessToken)
				.delete(ClientResponse.class);
		assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
	}


	private void deleteAccessToken()
	{
		ClientResponse response = webResource.path("accesstokens/" + accessToken)
				.header("Authorization", "Bearer " + adminCreds).delete(ClientResponse.class);
		assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
	}


	private void deleteUser()
	{
		ClientResponse response = webResource.path("users/" + userIdUrlSafe)
				.header("Authorization", "Bearer " + adminCreds).delete(ClientResponse.class);
		assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
	}


	private void deleteClient()
	{
		ClientResponse response = webResource.path("clients/" + clientId)
				.header("Authorization", "Bearer " + adminCreds).delete(ClientResponse.class);
		assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
	}
}
