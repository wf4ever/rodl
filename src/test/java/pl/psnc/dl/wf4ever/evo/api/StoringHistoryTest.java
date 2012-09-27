package pl.psnc.dl.wf4ever.evo.api;

import java.net.URI;

import junit.framework.Assert;

import org.junit.Test;
import pl.psnc.dl.wf4ever.evo.EvoTest;
import pl.psnc.dl.wf4ever.evo.EvoType;
import pl.psnc.dl.wf4ever.evo.JobStatus;

public class StoringHistoryTest extends EvoTest {

    protected URI ro2;
    protected String id = "ale-ma-mid";
    protected final String rdfFilePath = "foo/bar.rdf";
    
    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        ro2 = createRO(accessToken);
    }


    @Test
    public void testStoringHistory() throws InterruptedException {
        //@TODO create test structure by adding files and so on        
        JobStatus sp1Status = new JobStatus(ro, EvoType.SNAPSHOT, false);
        URI copyJob = createCopyJob(sp1Status).getLocation();
        sp1Status = getRemoteStatus(copyJob, WAIT_FOR_COPY);
        
        //addFile(accessToken,filePath, ro);
        
        JobStatus sp2Status = new JobStatus(ro, EvoType.ARCHIVED, false);
        copyJob = createCopyJob(sp2Status).getLocation();
        sp2Status = getRemoteStatus(copyJob, WAIT_FOR_COPY);
        
        String snaphot1Answer = webResource.path("evo/info/").queryParam("ro",sp1Status.getTarget().toString()).header("Authorization", "Bearer " + adminCreds).accept("text/turtle").get(String.class);
        String snapho2Answer = webResource.path("evo/info/").queryParam("ro",sp2Status.getTarget().toString()).header("Authorization", "Bearer " + adminCreds).accept("text/turtle").get(String.class);

        logger.info(snapho2Answer);
        
        Assert.assertEquals("Snapshot 1 should not contain any content",snaphot1Answer, "");
        Assert.assertTrue("Snaphot 2 shpuld contain Change Specification", snapho2Answer.contains("ChangeSpecification"));
        
    }

}