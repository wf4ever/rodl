package pl.psnc.dl.wf4ever.integration.evo;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.evo.CopyJobStatus;
import pl.psnc.dl.wf4ever.evo.CopyResource;
import pl.psnc.dl.wf4ever.integration.AbstractIntegrationTest;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;
import pl.psnc.dl.wf4ever.job.Job.State;
import pl.psnc.dl.wf4ever.job.JobStatus;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * @author piotrhol
 * @author filipwis
 * 
 */
@Category(IntegrationTest.class)
public class AbstractEvoTest extends AbstractIntegrationTest {

    public static final int WAIT_FOR_COPY = 2000;
    public static final int WAIT_FOR_FINALIZE = 2000;
    protected String filePath = "foobar";
    protected URI ro;
    protected final static Logger logger = Logger.getLogger(CopyResource.class);


    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        ro = createRO();
        addLoremIpsumFile(ro, filePath);
    }


    protected ClientResponse createCopyJob(JobStatus status, String target) {
        Builder builder = webResource.path("evo/copy/").header("Authorization", "Bearer " + accessToken);
        if (target != null) {
            builder = builder.header("Slug", target);
        }
        return builder.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, status);
    }


    protected ClientResponse createFinalizeJob(JobStatus status) {
        return webResource.path("evo/finalize/").header("Authorization", "Bearer " + accessToken)
                .type(MediaType.APPLICATION_JSON).post(ClientResponse.class, status);
    }


    protected CopyJobStatus getFinalizeJobStatus(URI job, JobStatus original) {
        return webResource.uri(job).header("Authorization", "Bearer " + accessToken).get(CopyJobStatus.class);
    }


    protected CopyJobStatus getRemoteStatus(URI job, int interval)
            throws InterruptedException {
        int cnt = 0;
        CopyJobStatus remoteStatus;
        do {
            remoteStatus = webResource.uri(job).header("Authorization", "Bearer " + accessToken)
                    .get(CopyJobStatus.class);
            synchronized (this) {
                wait(1000);
            }
        } while (remoteStatus.getState() == State.RUNNING && (cnt++) < interval);
        return remoteStatus;
    }

}
