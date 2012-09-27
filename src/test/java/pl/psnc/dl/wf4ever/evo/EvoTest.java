package pl.psnc.dl.wf4ever.evo;

import java.net.URI;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.APITest;
import pl.psnc.dl.wf4ever.evo.Job.State;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.WebAppDescriptor;

/**
 * @author piotrhol
 * @author filipwis
 * 
 */
public class EvoTest extends APITest {

    public static final int WAIT_FOR_COPY = 2000;
    public static final int WAIT_FOR_FINALIZE = 2000;
    protected String accessToken;
    protected String filePath = "foobar";
    protected final static Logger logger = Logger.getLogger(CopyResource.class);


    public EvoTest() {
        super(new WebAppDescriptor.Builder("pl.psnc.dl.wf4ever").build());
    }


    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        createUsers();
        accessToken = createAccessToken(userId);
        ro = createRO(accessToken, roUUID);
        //addFile(accessToken,filePath, ro);
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
