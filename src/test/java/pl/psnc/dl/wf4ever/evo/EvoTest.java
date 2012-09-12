package pl.psnc.dl.wf4ever.evo;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.evo.Job.State;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

/**
 * @author piotrhol
 * 
 */
public class EvoTest extends JerseyTest {

    public static final int WAIT_FOR_COPY = 100;
    public static final int WAIT_FOR_FINALIZE = 10;
    private final String clientName = "ROSRS testing app written in Ruby";
    private final String clientRedirectionURI = "OOB"; // will not be used
    private String clientId;
    private final String adminCreds = StringUtils.trim(Base64.encodeBase64String("wfadmin:wfadmin!!!".getBytes()));
    private final String userId = UUID.randomUUID().toString();
    private final String userIdUrlSafe = StringUtils.trim(Base64.encodeBase64URLSafeString(userId.getBytes()));
    private final String username = "John Doe";
    protected WebResource webResource;
    protected String accessToken;
    protected URI ro;
    protected String filePath = "foobar";
    protected final static Logger logger = Logger.getLogger(CopyResource.class);


    public EvoTest() {
        super(new WebAppDescriptor.Builder("pl.psnc.dl.wf4ever").build());
    }


    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        client().setFollowRedirects(true);
        if (resource().getURI().getHost().equals("localhost")) {
            webResource = resource();
        } else {
            webResource = resource().path("rodl/");
        }
        clientId = createClient();
        createUsers();
        accessToken = createAccessTokens();
        ro = createRO();
        addFile();
    }


    @Override
    public void tearDown()
            throws Exception {
        super.tearDown();
        deleteROs();
        deleteAccessTokens();
        deleteUsers();
        deleteClient();
    }


    /** Creating test structure */

    private String createClient() {
        ClientResponse response = webResource.path("clients/").header("Authorization", "Bearer " + adminCreds)
                .post(ClientResponse.class, clientName + "\r\n" + clientRedirectionURI);
        String clientId = response.getLocation().resolve(".").relativize(response.getLocation()).toString();
        response.close();
        return clientId;
    }


    private void createUsers() {
        webResource.path("users/" + userIdUrlSafe).header("Authorization", "Bearer " + adminCreds)
                .put(ClientResponse.class, username);
    }


    private String createAccessTokens() {
        ClientResponse response = webResource.path("accesstokens/").header("Authorization", "Bearer " + adminCreds)
                .post(ClientResponse.class, clientId + "\r\n" + userId);
        String accessToken = response.getLocation().resolve(".").relativize(response.getLocation()).toString();
        response.close();
        return accessToken;
    }


    private URI createRO() {
        String uuid = UUID.randomUUID().toString();
        return createRO(uuid);
    }


    protected URI createRO(String uuid) {
        ClientResponse response = webResource.path("ROs/").header("Authorization", "Bearer " + accessToken)
                .header("Slug", uuid).post(ClientResponse.class);
        URI ro = response.getLocation();
        response.close();
        return ro;
    }


    private void addFile() {
        webResource.uri(ro).header("Slug", filePath).header("Authorization", "Bearer " + accessToken)
                .type("text/plain").post(String.class, "lorem ipsum");

    }


    private void deleteROs() {
        String list = webResource.path("ROs/").header("Authorization", "Bearer " + accessToken).get(String.class);
        if (!list.isEmpty()) {
            String[] ros = list.trim().split("\r\n");
            for (String ro : ros) {
                webResource.uri(URI.create(ro)).header("Authorization", "Bearer " + accessToken).delete();
            }
        }
    }


    private void deleteAccessTokens() {
        webResource.path("accesstokens/" + accessToken).header("Authorization", "Bearer " + adminCreds).delete();
    }


    private void deleteUsers() {
        webResource.path("users/" + userIdUrlSafe).header("Authorization", "Bearer " + adminCreds).delete();
    }


    private void deleteClient() {
        webResource.path("clients/" + clientId).header("Authorization", "Bearer " + adminCreds).delete();
    }


    protected ClientResponse createCopyJob(JobStatus status) {
        return webResource.path("evo/copy/").header("Authorization", "Bearer " + accessToken)
                .type(MediaType.APPLICATION_JSON).post(ClientResponse.class, status);
    }


    protected ClientResponse createFinalizeJob(JobStatus status) {
        return webResource.path("evo/finalize/").header("Authorization", "Bearer " + accessToken)
                .type(MediaType.APPLICATION_JSON).post(ClientResponse.class, status);
    }


    protected JobStatus getFinalizeJobStatus(URI job, JobStatus original) {
        return webResource.uri(job).header("Authorization", "Bearer " + accessToken).get(JobStatus.class);
    }
    
    protected JobStatus getRemoteStatus(URI job, int interval)
            throws InterruptedException {
        int cnt = 0;
        JobStatus remoteStatus;
        do {
            remoteStatus = webResource.uri(job).header("Authorization", "Bearer " + accessToken).get(JobStatus.class);
            synchronized (this) {
                wait(1000);
            }
        } while (remoteStatus.getState() == State.RUNNING && (cnt++) < interval);
        return remoteStatus;
    }
 
}
