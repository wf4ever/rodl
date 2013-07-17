package pl.psnc.dl.wf4ever.evo;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.IntegrationTest;
import pl.psnc.dl.wf4ever.evo.Job.State;

@Category(IntegrationTest.class)
public class StoringHistoryTest extends EvoTest {

    protected URI ro2;
    protected String oldResourceFile = "oldREsourceFile";
    protected String newResourceFile = "newREsourceFile";
    protected String modifiedResourceFile = "modifiedREsourceFile";


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
    public void testStoringHistory()
            throws InterruptedException, IOException {
        //@TODO improve the text structure;

        addFile(ro, oldResourceFile, accessToken);
        URI fullModificatedFilePath = addFile(ro, modifiedResourceFile, accessToken).getLocation();

        CopyJobStatus sp1Status = new CopyJobStatus(ro, EvoType.SNAPSHOT, true);
        URI copyJob = createCopyJob(sp1Status, null).getLocation();
        sp1Status = getRemoteStatus(copyJob, WAIT_FOR_COPY);
        Assert.assertEquals(State.DONE, sp1Status.getState());

        addFile(ro, newResourceFile, accessToken);
        removeFile(ro, oldResourceFile, accessToken);
        updateFile(fullModificatedFilePath, accessToken);
        String content = webResource.uri(ro).path(modifiedResourceFile)
                .header("Authorization", "Bearer " + accessToken).get(String.class);

        CopyJobStatus sp2Status = new CopyJobStatus(ro, EvoType.SNAPSHOT, true);
        copyJob = createCopyJob(sp2Status, null).getLocation();
        sp2Status = getRemoteStatus(copyJob, WAIT_FOR_COPY);
        Assert.assertEquals(State.DONE, sp2Status.getState());

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

        assertTrue("sp2 should contain Additon", sp2Evo.contains("Addition"));
        assertTrue("sp2 should contain Removal", sp2Evo.contains("Removal"));
        assertTrue("sp2 should contain Modification", sp2Evo.contains("Modification"));
    }
}
