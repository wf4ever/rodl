package pl.psnc.dl.wf4ever.evo.api;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;

import org.junit.Test;

import pl.psnc.dl.wf4ever.common.EvoType;
import pl.psnc.dl.wf4ever.evo.EvoTest;
import pl.psnc.dl.wf4ever.evo.JobStatus;

public class StoringHistoryTest extends EvoTest {

    protected URI ro2;
    protected String oldResourceFile = "oldREsourceFile";
    protected String newResourceFile = "newREsourceFile";
    protected String modifiedResourceFile = "modifiedREsourceFile";


    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        //ro2 = createRO(accessToken);
    }


    @Override
    public void tearDown()
            throws Exception {
        super.tearDown();
    }


    @Test
    public void testStoringHistory()
            throws InterruptedException, IOException {
        //@TODO improve the text structure;

        addFile(ro, oldResourceFile, accessToken);
        addFile(ro, modifiedResourceFile, accessToken);

        JobStatus sp1Status = new JobStatus(ro, EvoType.SNAPSHOT, true);
        URI copyJob = createCopyJob(sp1Status).getLocation();
        sp1Status = getRemoteStatus(copyJob, WAIT_FOR_COPY);

        addFile(ro, newResourceFile, accessToken);
        removeFile(ro, oldResourceFile, accessToken);

        JobStatus sp2Status = new JobStatus(ro, EvoType.SNAPSHOT, true);
        copyJob = createCopyJob(sp2Status).getLocation();
        sp2Status = getRemoteStatus(copyJob, WAIT_FOR_COPY);

        String roManifest = webResource.uri(ro).path("/.ro/manifest.rdf")
                .header("Authorization", "Bearer " + accessToken).accept("text/turtle").get(String.class);
        String sp1Manifest = webResource.uri(sp1Status.getTarget()).path("/.ro/manifest.rdf")
                .header("Authorization", "Bearer " + accessToken).accept("text/turtle").get(String.class);
        String sp2Manifest = webResource.uri(sp2Status.getTarget()).path("/.ro/manifest.rdf")
                .header("Authorization", "Bearer " + accessToken).accept("text/turtle").get(String.class);

        String roEvo = webResource.path("/evo/info").queryParam("ro", sp1Status.getCopyfrom().toString())
                .header("Authorization", "Bearer " + accessToken).accept("text/turtle").get(String.class);
        String sp1Evo = webResource.path("/evo/info").queryParam("ro", sp1Status.getTarget().toString())
                .header("Authorization", "Bearer " + accessToken).accept("text/turtle").get(String.class);
        String sp2Evo = webResource.path("/evo/info").queryParam("ro", sp2Status.getTarget().toString())
                .header("Authorization", "Bearer " + accessToken).accept("text/turtle").get(String.class);

        System.out.println(sp1Evo);
        System.out.println(sp2Evo);

        assertTrue("sp2 should contain Additon", sp2Evo.contains("Addition"));
        assertTrue("sp2 should contain Removal", sp2Evo.contains("Removal"));

    }
}
