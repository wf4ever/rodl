package pl.psnc.dl.wf4ever.resources;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.IntegrationTest;
import pl.psnc.dl.wf4ever.job.Job.State;
import pl.psnc.dl.wf4ever.job.JobStatus;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.util.SafeURI;
import pl.psnc.dl.wf4ever.vocabulary.AO;
import pl.psnc.dl.wf4ever.vocabulary.FOAF;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.sun.jersey.api.client.ClientResponse;

@Category(IntegrationTest.class)
public class ResourceTest extends ResourceBase {

    protected String createdFromZipResourceObject = UUID.randomUUID().toString();
    protected Integer maxSeonds = 100;


    @Override
    public void setUp()
            throws Exception {
        super.setUp();
    }


    @Override
    public void tearDown()
            throws Exception {
        super.tearDown();
    }


    @Test
    public void testGetROList() {
        String list = webResource.path("ROs").header("Authorization", "Bearer " + accessToken).get(String.class);
        assertTrue(list.contains(ro.toString()));
        assertTrue(list.contains(ro2.toString()));
    }


    @Test
    public void testGetROWithWhitespaces() {
        URI ro = createRO("ro " + UUID.randomUUID().toString(), accessToken);
        String list = webResource.path("ROs").header("Authorization", "Bearer " + accessToken).get(String.class);
        assertTrue(list.contains(ro.toString()));
    }


    @Test
    public void testGetROMetadata()
            throws URISyntaxException {
        client().setFollowRedirects(false);
        ClientResponse response = webResource.uri(ro).accept("text/turtle").get(ClientResponse.class);
        assertTrue(response
                .getHeaders()
                .get(Constants.LINK_HEADER)
                .contains(
                    "<" + webResource.path("/evo/info").queryParam("ro", ro.toString()).toString()
                            + ">; rel=\"http://www.openarchives.org/ore/terms/isDescribedBy\""));

        assertEquals(HttpServletResponse.SC_SEE_OTHER, response.getStatus());
        assertEquals(webResource.uri(ro).path(".ro/manifest.rdf").getURI().getPath(), response.getLocation().getPath());
        response.close();
    }


    @Test
    public void testGetROHTML()
            throws URISyntaxException {
        client().setFollowRedirects(false);
        ClientResponse response = webResource.uri(ro).path("/").accept("text/html").get(ClientResponse.class);
        assertEquals(HttpServletResponse.SC_SEE_OTHER, response.getStatus());
        URI portalURI = new URI("http", "sandbox.wf4ever-project.org", "/portal/ro", "ro="
                + webResource.uri(ro).getURI().toString(), null);
        assertEquals(portalURI.getPath(), response.getLocation().getPath());
        assertTrue(portalURI.getQuery().contains("ro="));
        response.close();
    }


    @Test
    public void testGetROZip() {
        client().setFollowRedirects(false);
        ClientResponse response = webResource.uri(ro).accept("application/zip").get(ClientResponse.class);
        assertEquals(HttpServletResponse.SC_SEE_OTHER, response.getStatus());
        assertEquals(webResource.path("zippedROs").path(ro.toString().split("ROs")[1]).getURI().getPath(), response
                .getLocation().getPath());
        response.close();

        response = webResource.path("zippedROs").path(ro.toString().split("ROs")[1]).get(ClientResponse.class);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/zip", response.getType().toString());
        response.close();

        response = webResource.path("zippedROs").path(ro.toString().split("ROs")[1])
                .accept("text/html;q=0.9,*/*;q=0.8").get(ClientResponse.class);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/zip", response.getType().toString());
        response.close();
    }


    @Test
    public void storeROFromZip()
            throws IOException, ClassNotFoundException, NamingException, InterruptedException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/ro1.zip");
        ClientResponse response = webResource.path("ROs/zip/store").accept("application/json")
                .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                .type("application/zip").post(ClientResponse.class, is);
        assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
            response.getStatus());
        response.close();
        JobStatus status = getStatus(response.getLocation());
        Assert.assertEquals(State.DONE, status.getState());
        response = webResource.uri(status.getTarget()).accept("application/zip")
                .header("Authorization", "Bearer " + accessToken).get(ClientResponse.class);
        is = response.getEntity(InputStream.class);
        File file = File.createTempFile("storeFromZipTests", "zip");
        FileOutputStream outputStream = new FileOutputStream(file);
        IOUtils.copy(is, outputStream);
        is.close();
        outputStream.close();
        ZipFile zip = new ZipFile(file);
        Enumeration<? extends ZipEntry> e = zip.entries();
        List entries = new ArrayList<String>();
        while (e.hasMoreElements()) {
            entries.add(e.nextElement().getName());
        }
        Assert.assertTrue(entries.contains("conclusion.pdf"));
        Assert.assertTrue(entries.contains(".ro/manifest.rdf"));
        Assert.assertTrue(entries.contains(".ro/evo_info.ttl"));
        Assert.assertTrue(entries.contains("Hypothesis.txt"));
        file.delete();
    }


    @Test
    public void storeWrongROFromZip()
            throws IOException, ClassNotFoundException, NamingException, InterruptedException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/wrong.zip");
        ClientResponse response = webResource.path("ROs/zip/store").accept("application/json")
                .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                .type("application/zip").post(ClientResponse.class, is);
        assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
            response.getStatus());
        response.close();
        JobStatus status = getStatus(response.getLocation());
        Assert.assertEquals(State.SERVICE_ERROR, status.getState());
    }


    @Test
    public void createROFromZipWithWhitespaces()
            throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/white_spaces_ro.zip");
        ClientResponse response = webResource.path("ROs/zip/store").accept("application/json")
                .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                .type("application/zip").post(ClientResponse.class, is);
        assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
            response.getStatus());
        response.close();
        JobStatus status = getStatus(response.getLocation());
        Assert.assertEquals(State.DONE, status.getState());
    }


    @Test
    public void createROFromZipWithEvoAnnotation()
            throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/zip_with_evo.zip");
        ClientResponse response = webResource.path("ROs/zip/store").accept("application/json")
                .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                .type("application/zip").post(ClientResponse.class, is);
        assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
            response.getStatus());
        response.close();
        JobStatus status = getStatus(response.getLocation());
        Assert.assertEquals(State.DONE, status.getState());
    }


    @Test
    public void createConflictedROFromZip()
            throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/ro1.zip");
        ClientResponse response1 = webResource.path("ROs/zip/store").accept("application/json")
                .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                .type("application/zip").post(ClientResponse.class, IOUtils.toByteArray(is));
        assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
            response1.getStatus());
        JobStatus status1 = getStatus(response1.getLocation());
        Assert.assertEquals(State.DONE, status1.getState());

        is = getClass().getClassLoader().getResourceAsStream("singleFiles/ro1.zip");
        ClientResponse response2 = webResource.path("ROs/zip/store").accept("application/json")
                .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                .type("application/zip").post(ClientResponse.class, IOUtils.toByteArray(is));

        JobStatus status2 = getStatus(response2.getLocation());
        Assert.assertEquals(State.DONE, status2.getState());

        Assert.assertNotEquals(status1.getTarget(), status2.getTarget());

    }


    @Test
    public void updateEvoInfo() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("manifest.ttl");
        ClientResponse response = webResource.uri(ro).path("/.ro/evo_info.ttl")
                .header("Authorization", "Bearer " + accessToken).type("text/turtle").put(ClientResponse.class, is);
        assertEquals("Updating evo_info should be protected", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        response.close();
        Builder builder = new Builder(null, DatasetFactory.createMem(), false);
        ResearchObject researchObject = builder.buildResearchObject(ro);
        OntModel manifestModel = ModelFactory.createOntologyModel();
        manifestModel.read(researchObject.getManifestUri().toString());

        Resource bodyR = manifestModel.createResource(SafeURI.URItoString(researchObject
                .getFixedEvolutionAnnotationBodyUri()));
        List<Statement> anns = manifestModel.listStatements(null, AO.body, bodyR).toList();
        assertTrue("Cannot find annotation", !anns.isEmpty());
        URI annUri = URI.create(anns.get(0).getSubject().getURI());
        response = webResource.uri(annUri).header("Authorization", "Bearer " + accessToken)
                .delete(ClientResponse.class);
        response.close();

        assertEquals("Removing evo info should be protected", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        response = webResource.uri(researchObject.getFixedEvolutionAnnotationBodyUri())
                .header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
        assertEquals("Removing evo info should be protected", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        response.close();
    }


    @Test
    public void testFillCreatorNames() {
        URI ro = createRO("ro " + UUID.randomUUID().toString(), accessToken);
        Builder builder = new Builder(null, DatasetFactory.createMem(), false);
        ResearchObject researchObject = builder.buildResearchObject(ro);
        Thing manifest = researchObject.getManifest();
        OntModel manifestModel = ModelFactory.createOntologyModel();
        manifestModel.read(researchObject.getManifestUri().toString());
        for (RDFNode n : manifestModel.listObjectsOfProperty(DCTerms.creator).toList()) {
            Assert.assertNotNull(manifestModel.getProperty(manifestModel.getResource(n.toString()), FOAF.name));
        }

    }


    @Test
    public void shouldReturn410GoneAfterDelete() {
        URI ro = createRO("ro " + UUID.randomUUID().toString(), accessToken);
        ClientResponse response = webResource.uri(ro).delete(ClientResponse.class);
        assertThat(response.getStatus(), equalTo(HttpStatus.SC_NO_CONTENT));
        response = webResource.uri(ro).get(ClientResponse.class);
        assertThat(response.getStatus(), equalTo(HttpStatus.SC_GONE));
    }


    @Test
    public void shouldReturn404NotFoundAfterPurgeDelete() {
        URI ro = createRO("ro " + UUID.randomUUID().toString(), accessToken);
        ClientResponse response = webResource.uri(ro).header("Purge", true).delete(ClientResponse.class);
        assertThat(response.getStatus(), equalTo(HttpStatus.SC_NO_CONTENT));
        response = webResource.uri(ro).get(ClientResponse.class);
        assertThat(response.getStatus(), equalTo(HttpStatus.SC_NOT_FOUND));
    }


    @Test
    public void shouldAllowToPurgeDeleteAfterDelete() {
        URI ro = createRO("ro " + UUID.randomUUID().toString(), accessToken);
        ClientResponse response = webResource.uri(ro).delete(ClientResponse.class);
        assertThat(response.getStatus(), equalTo(HttpStatus.SC_NO_CONTENT));
        response = webResource.uri(ro).header("Purge", true).delete(ClientResponse.class);
        assertThat(response.getStatus(), equalTo(HttpStatus.SC_NO_CONTENT));
        response = webResource.uri(ro).get(ClientResponse.class);
        assertThat(response.getStatus(), equalTo(HttpStatus.SC_NOT_FOUND));
    }


    @Test
    public void shouldReturn404NotFoundWhenAskingForHtml() {
        URI ro = createRO("ro " + UUID.randomUUID().toString(), accessToken);
        ClientResponse response = webResource.uri(ro).header("Purge", true).delete(ClientResponse.class);
        assertThat(response.getStatus(), equalTo(HttpStatus.SC_NO_CONTENT));
        response = webResource.uri(ro).accept(MediaType.TEXT_HTML).get(ClientResponse.class);
        assertThat(response.getStatus(), equalTo(HttpStatus.SC_NOT_FOUND));
    }


    private JobStatus getStatus(URI jobUri)
            throws InterruptedException {
        int counter = 0;
        while (counter < maxSeonds) {
            Thread.sleep(1000);
            JobStatus status = webResource.uri(jobUri).accept("application/json").get(JobStatus.class);
            if (status.getState() != State.RUNNING) {
                return status;
            }
        }
        return null;

    }

}
