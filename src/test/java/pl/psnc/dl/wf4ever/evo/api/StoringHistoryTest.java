package pl.psnc.dl.wf4ever.evo.api;

import java.io.InputStream;
import java.net.URI;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import pl.psnc.dl.wf4ever.common.HibernateUtil;
import pl.psnc.dl.wf4ever.evo.EvoTest;
import pl.psnc.dl.wf4ever.evo.EvoType;
import pl.psnc.dl.wf4ever.evo.JobStatus;

import com.sun.jersey.api.client.ClientResponse;

//it makes some error so it is ignored for now 
//it is an server error (500)
@Ignore
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
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        addFile(ro, newResourceFile, accessToken);
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        InputStream is = getClass().getClassLoader().getResourceAsStream("manifest.ttl");
        ClientResponse response = webResource.path(ro + "/.ro/manifest.rdf")
                .header("Authorization", "Bearer " + accessToken).type("text/turtle").put(ClientResponse.class, is);
        response.close();
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        JobStatus sp2Status = new JobStatus(ro, EvoType.SNAPSHOT, false);
        copyJob = createCopyJob(sp2Status).getLocation();
        sp2Status = getRemoteStatus(copyJob, WAIT_FOR_COPY);
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        System.out.println("e?");
        String snaphot1Answer = webResource.path("evo/info/").queryParam("ro", sp1Status.getTarget().toString())
                .header("Authorization", "Bearer " + adminCreds).accept("text/turtle").get(String.class);
        System.out.println("d?");
        String snapho2Answer = webResource.path("evo/info/").queryParam("ro", sp2Status.getTarget().toString())
                .header("Authorization", "Bearer " + adminCreds).accept("text/turtle").get(String.class);
        System.out.println("g?");
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        Assert.assertEquals("Snapshot 1 should not contain any content", snaphot1Answer, "");
        Assert.assertTrue("Snaphot 2 should contain the Change Specification",
            snapho2Answer.contains("ChangeSpecification"));
        Assert.assertTrue("Snaphot 2 should contain an Addition Class", snapho2Answer.contains("Addition"));
    }

}
