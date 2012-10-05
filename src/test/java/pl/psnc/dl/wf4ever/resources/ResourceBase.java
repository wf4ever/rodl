package pl.psnc.dl.wf4ever.resources;

import java.util.ArrayList;
import java.util.List;

import com.sun.jersey.test.framework.WebAppDescriptor;

import pl.psnc.dl.wf4ever.W4ETest;

public class ResourceBase extends W4ETest{
    protected List<String> linkHeadersR = new ArrayList<>();
    private final String externalResource = "http://example.com/external/resource.txt";

    
    public ResourceBase() {
        super(new WebAppDescriptor.Builder("pl.psnc.dl.wf4ever").build());
        createLinkHeaders();
        createUserWithAnswer(userIdSafe, username).close();
        accessToken = createAccessToken(userId);
        ro = createRO(accessToken);
        ro2 = createRO(accessToken);
    }
    
    @Override
    protected void finalize()
            throws Throwable {
        deleteROs();
        deleteAccessToken(accessToken);
        deleteUser(userIdSafe);
        super.finalize();
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
}
