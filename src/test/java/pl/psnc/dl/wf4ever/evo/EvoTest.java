package pl.psnc.dl.wf4ever.evo;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.junit.Ignore;

import pl.psnc.dl.wf4ever.W4ETest;
import pl.psnc.dl.wf4ever.evo.Job.State;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.WebAppDescriptor;

/**
 * @author piotrhol
 * @author filipwis
 * 
 */
@Ignore
public class EvoTest extends W4ETest {

    public static final int WAIT_FOR_COPY = 2000;
    public static final int WAIT_FOR_FINALIZE = 2000;
    protected String filePath = "foobar";
    protected final static Logger logger = Logger.getLogger(CopyResource.class);


    public EvoTest() {
        super(new WebAppDescriptor.Builder("pl.psnc.dl.wf4ever").build());
    }


    @Override
    public void setUp()
            throws Exception {
        createUserWithAnswer(userIdSafe, username).close();
        createUserWithAnswer(userId2Safe, username2).close();
        accessToken = createAccessToken(userId);
        ro = createRO(accessToken);
        addFile(ro, filePath, accessToken);
        super.setUp();
    }


    @Override
    public void tearDown()
            throws Exception {
        deleteROs();
        deleteAccessToken(accessToken);
        deleteUser(userIdSafe);
        deleteUser(userId2Safe);
        super.tearDown();
    }


    @Override
    protected void finalize()
            throws Throwable {
        super.finalize();
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
