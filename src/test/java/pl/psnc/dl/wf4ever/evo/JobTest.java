package pl.psnc.dl.wf4ever.evo;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;

import pl.psnc.dl.wf4ever.common.EvoType;
import pl.psnc.dl.wf4ever.evo.Job.State;

import com.sun.jersey.api.client.ClientResponse;

public class JobTest extends EvoTest {

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
    public final void testCopyJobCreation()
            throws InterruptedException {
        ClientResponse response = createCopyJob(new JobStatus(ro, EvoType.SNAPSHOT, true));
        URI copyJob = response.getLocation();
        getRemoteStatus(copyJob, WAIT_FOR_COPY);
        assertEquals(response.getEntity(String.class), HttpServletResponse.SC_CREATED, response.getStatus());

    }


    @Test
    public final void testCopyJobStatusDataIntegrity()
            throws InterruptedException {
        JobStatus status = new JobStatus(ro, EvoType.SNAPSHOT, true);
        status.setTarget("testTarget");
        URI copyJob = createCopyJob(status).getLocation();
        JobStatus remoteStatus = getRemoteStatus(copyJob, WAIT_FOR_COPY);
        String s = webResource.path(copyJob.getPath()).header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON).get(String.class);
        Assert.assertTrue(s.contains("testTarget"));
        Assert.assertEquals(status.getCopyfrom(), remoteStatus.getCopyfrom());
        Assert.assertEquals(status.getType(), remoteStatus.getType());
        Assert.assertEquals(status.isFinalize(), remoteStatus.isFinalize());
    }


    @Test
    public final void testJobFinalization()
            throws InterruptedException {
        URI copyJob = createCopyJob(new JobStatus(ro, EvoType.SNAPSHOT, false)).getLocation();
        JobStatus status = getRemoteStatus(copyJob, WAIT_FOR_COPY);
        JobStatus status2 = new JobStatus();
        status2.setTarget(status.getTarget());
        URI finalizeJob = createFinalizeJob(status2).getLocation();
        JobStatus remoteStatus = getRemoteStatus(finalizeJob, WAIT_FOR_FINALIZE);
        Assert.assertEquals(State.DONE, remoteStatus.getState());
        //OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        //model.read(status.getTarget().toString());
        //TODO verify correct finalized RO
    }


    @Test
    public final void testCopyAndFinalizationJob()
            throws InterruptedException {
        JobStatus status = new JobStatus(ro, EvoType.SNAPSHOT, true);
        URI copyAndFinalizeJob = createCopyJob(status).getLocation();
        JobStatus remoteStatus = getRemoteStatus(copyAndFinalizeJob, WAIT_FOR_COPY + WAIT_FOR_FINALIZE);
        Assert.assertEquals(remoteStatus.toString(), State.DONE, remoteStatus.getState());
        //OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        //model.read(status.getTarget().toString());
        //TODO verify correct finalized RO
    }


    //negative scenarios

    /**
     * In case there is no token in request, 401 Unauthorized should be returned.
     */
    @Test
    public final void createCopyJobWithNoToken() {

    }


    /**
     * In case given token doesn't exists, 401 Unauthorized should be returned.
     */
    @Test
    public final void createCoptJobWithWrongToken() {

    }
}
