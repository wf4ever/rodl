package pl.psnc.dl.wf4ever.resources;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;

import pl.psnc.dl.wf4ever.W4ETest;
import pl.psnc.dl.wf4ever.common.HibernateUtil;
import pl.psnc.dl.wf4ever.common.ResearchObject;

import com.sun.jersey.test.framework.WebAppDescriptor;

@Ignore
public class ResourceBase extends W4ETest {

    protected List<String> linkHeadersR = new ArrayList<>();
    private final String externalResource = "http://example.com/external/resource.txt";


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
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        ro = createRO(accessToken);
        ro2 = createRO(accessToken);
    }


    @Override
    public void tearDown()
            throws Exception {
        deleteROs();
        deleteAccessToken(accessToken);
        deleteUser(userIdSafe);
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
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
}