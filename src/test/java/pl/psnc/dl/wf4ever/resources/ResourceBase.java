package pl.psnc.dl.wf4ever.resources;

import java.util.ArrayList;
import java.util.List;

import pl.psnc.dl.wf4ever.W4ETest;
import pl.psnc.dl.wf4ever.common.ResearchObject;

import com.sun.jersey.test.framework.WebAppDescriptor;

public class ResourceBase extends W4ETest {

    protected List<String> linkHeadersR = new ArrayList<>();


    public ResourceBase() {
        super(new WebAppDescriptor.Builder("pl.psnc.dl.wf4ever").build());
        createLinkHeaders();
    }


    @Override
    protected void finalize()
            throws Throwable {
        super.finalize();
    }


    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        createUserWithAnswer(userIdSafe, username).close();
        accessToken = createAccessToken(userId);
        ro = createRO(accessToken);
        ro2 = createRO(accessToken);
    }


    @Override
    public void tearDown()
            throws Exception {
        deleteROs();
        deleteAccessToken(accessToken);
        deleteUser(userIdSafe);
        super.tearDown();
    }


    /*
     * helpers
     */
    private void createLinkHeaders() {
        linkHeadersR.clear();
        linkHeadersR.add("<" + resource().getURI() + "ROs/r/.ro/manifest.rdf>; rel=bookmark");
        linkHeadersR.add("<" + resource().getURI() + "zippedROs/r/>; rel=bookmark");
        linkHeadersR.add("<http://sandbox.wf4ever-project.org/portal/ro?ro=" + resource().getURI()
                + "ROs/r/>; rel=bookmark");
    }


    protected String getManifest(ResearchObject ro) {
        return webResource.uri(ro.getManifestUri()).header("Authorization", "Bearer " + accessToken).get(String.class);
    }


    protected String getResourceToString(ResearchObject ro, String resourceRelativePath) {
        return webResource.uri(ro.getUri()).path(resourceRelativePath).header("Authorization", "Bearer " + accessToken)
                .get(String.class);
    }
}
