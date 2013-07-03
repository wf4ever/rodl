package pl.psnc.dl.wf4ever.monitoring;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import pl.psnc.dl.wf4ever.darceo.client.DArceoClient;
import pl.psnc.dl.wf4ever.darceo.client.DArceoException;
import pl.psnc.dl.wf4ever.db.dao.ResearchObjectPreservationStatusDAO;
import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.preservation.ResearchObjectPreservationStatus;
import pl.psnc.dl.wf4ever.preservation.Status;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectSerializable;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;

public class PreservationJobTest {

    JobExecutionContext context;
    URI checklistNotificationsUri;

    Dataset dataset;
    UserMetadata userProfile;
    Builder builder;
    URI roUri = URI.create("http://www.example.com/ROs/preservationJobTest/" + UUID.randomUUID().toString() + "/");
    ResearchObjectPreservationStatusDAO preservationDAO;


    @Before
    public void setUp()
            throws IOException {
        context = Mockito.mock(JobExecutionContext.class);
        Properties properties = new Properties();
        dataset = DatasetFactory.createMem();
        userProfile = new UserMetadata("jank", "Jan Kowalski", Role.AUTHENTICATED, URI.create("http://jank"));
        builder = new Builder(userProfile, dataset, false);
        preservationDAO = new ResearchObjectPreservationStatusDAO();
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().begin();
    }


    public void tearDown() {
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
    }


    @Ignore
    @Test
    public void testJobExecute()
            throws JobExecutionException, DArceoException, IOException, InterruptedException {
        ResearchObject.create(builder, roUri);
        ResearchObjectPreservationStatus status = preservationDAO.findById(roUri.toString());
        Assert.assertEquals(Status.NEW, status.getStatus());
        //prepare job
        PreservationJob job = new PreservationJob();
        job.setBuilder(builder);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(PreservationJob.RESEARCH_OBJECT_URI, roUri);
        context = Mockito.mock(JobExecutionContext.class);
        Mockito.when(context.getMergedJobDataMap()).thenReturn(jobDataMap);
        job.execute(context);
        Thread.sleep(100);
        ResearchObjectSerializable returnedRo = DArceoClient.getInstance().getBlocking(roUri);
        Assert.assertNotNull(returnedRo);
    }

}
