package pl.psnc.dl.wf4ever.evo.api;

import java.net.URI;

import org.junit.Test;

import pl.psnc.dl.wf4ever.evo.EvoTest;
import pl.psnc.dl.wf4ever.evo.EvoType;
import pl.psnc.dl.wf4ever.evo.JobStatus;

public class StoringHistoryTest extends EvoTest {

    protected URI ro2;
    protected String id = "ale-ma-mid";
    
    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        ro2 = createRO(accessToken);
    }


    //@Test
    public void testStoringHistory() throws InterruptedException {
        JobStatus status = new JobStatus(ro, EvoType.SNAPSHOT, false);
        URI copyJob = createCopyJob(status).getLocation();
        status = getRemoteStatus(copyJob, WAIT_FOR_COPY);
        
        //status = new JobStatus(ro, EvoType.SNAPSHOT, false);
        //copyJob = createCopyJob(status).getLocation();
        //getRemoteStatus(copyJob, WAIT_FOR_COPY);
        
        //status = new JobStatus(ro, EvoType.ARCHIVED, false);
        //copyJob = createCopyJob(status).getLocation();
        //getRemoteStatus(copyJob, WAIT_FOR_COPY);
        
        //status = new JobStatus(ro2, EvoType.SNAPSHOT, false);
        //copyJob = createCopyJob(status).getLocation();
        //getRemoteStatus(copyJob, WAIT_FOR_COPY);
        
        
    }

}