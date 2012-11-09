package pl.psnc.dl.wf4ever.evo.api;

import java.io.InputStream;
import java.net.URI;

import org.junit.Test;

import pl.psnc.dl.wf4ever.common.EvoType;
import pl.psnc.dl.wf4ever.evo.EvoTest;
import pl.psnc.dl.wf4ever.evo.JobStatus;

import com.sun.jersey.api.client.ClientResponse;

public class StoringHistoryTest extends EvoTest {

    protected URI ro2;
    protected String newResourceFile = "newREsourceFile";


    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        ro2 = createRO(accessToken);
    }


    @Override
    public void tearDown()
            throws Exception {
        super.tearDown();
    }


    @Test
    public void testStoringHistory()
            throws InterruptedException {
        //@TODO improve the text structure;

        JobStatus sp1Status = new JobStatus(ro, EvoType.SNAPSHOT, false);
        URI copyJob = createCopyJob(sp1Status).getLocation();
        sp1Status = getRemoteStatus(copyJob, WAIT_FOR_COPY);

        addFile(ro, newResourceFile, accessToken);

        InputStream is = getClass().getClassLoader().getResourceAsStream("manifest.ttl");
        ClientResponse response = webResource.path(ro + "/.ro/manifest.rdf")
                .header("Authorization", "Bearer " + accessToken).type("text/turtle").put(ClientResponse.class, is);
        response.close();

        JobStatus sp2Status = new JobStatus(ro, EvoType.SNAPSHOT, false);
        copyJob = createCopyJob(sp2Status).getLocation();
        sp2Status = getRemoteStatus(copyJob, WAIT_FOR_COPY);

        String snapshot1Answer = webResource.uri(sp1Status.getTarget()).path("/.ro/manifest.rdf")
                .header("Authorization", "Bearer " + accessToken).accept("application/x-trig").get(String.class);
        String snapshot2Answer = webResource.uri(sp2Status.getTarget()).path("/.ro/manifest.rdf")
                .header("Authorization", "Bearer " + accessToken).accept("application/x-trig").get(String.class);

        System.out.println("-------------");
        System.out.println(snapshot1Answer);
        System.out.println("-------------");
        System.out.println(snapshot2Answer);
        System.out.println("-------------");

        //Assert.assertEquals("Snapshot 1 should not contain any content", snapshot1Answer, "");
        //Assert.assertTrue("Snaphot 2 should contain the Change Specification",
        //    snapshot2Answer.contains("ChangeSpecification"));
        //Assert.assertTrue("Snaphot 2 should contain an Addition Class", snapshot2Answer.contains("Addition"));
    }

}
