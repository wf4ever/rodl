package pl.psnc.dl.wf4ever.evo;

import java.net.URI;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import pl.psnc.dl.wf4ever.common.EvoType;
import pl.psnc.dl.wf4ever.evo.Job.State;

//it makas an error
@Ignore
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
        /*
        System.out.println("1");
        ClientResponse response = createCopyJob(new JobStatus(ro, EvoType.SNAPSHOT, false));
        System.out.println("2");
        URI copyJob = response.getLocation();
        System.out.println("3");
        getRemoteStatus(copyJob, WAIT_FOR_COPY);
        System.out.println("4");
        assertEquals(response.getEntity(String.class), HttpServletResponse.SC_CREATED, response.getStatus());
        System.out.println("5");
        */
        //to finish all operation before object will be removed
    }


    @Test
    public final void testCopyJobStatusDataIntegrity()
            throws InterruptedException {
        JobStatus status = new JobStatus(ro, EvoType.SNAPSHOT, false);
        URI copyJob = createCopyJob(status).getLocation();
        JobStatus remoteStatus = getRemoteStatus(copyJob, WAIT_FOR_COPY);
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

}
