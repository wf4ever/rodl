package pl.psnc.dl.wf4ever.monitoring;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import pl.psnc.dl.wf4ever.db.dao.AtomFeedEntryDAO;
import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
import pl.psnc.dl.wf4ever.notifications.Notification;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class StabilityFeedAggregationJobTest {

    JobExecutionContext context;
    static final URI RO_URI = URI.create("http://www.example.com/ro/test-ro/");
    static final URI EMPTY_RO_URI = URI.create("http://www.example.com/ro/test-empty-ro/");
    URI checklistNotificationsUri;
    protected static final String HOST_STRING = "http://127.0.0.1:8089";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);
    AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
    URI processedRO = URI.create("http://127.0.0.1:8089/rodl/ROs/SimpleRO/");


    @Before
    public void setUp()
            throws IOException {
        context = Mockito.mock(JobExecutionContext.class);
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("connection.properties"));
        } catch (IOException e) {
            throw new IOException("Configuration couldn't be loaded", e);
        }
        checklistNotificationsUri = URI.create(properties.getProperty("checklist_service_url"));
        //http mock
        InputStream checklistRefactorInput = StabilityFeedAggregationJobTest.class.getClassLoader()
                .getResourceAsStream("monitoring/stability_service_notification.xml");
        InputStream checklistRefactorNoEntryInput = StabilityFeedAggregationJobTest.class.getClassLoader()
                .getResourceAsStream("monitoring/stability_service_notification_case_empty.xml");

        stubFor(get(urlMatching((checklistNotificationsUri.toString() + ".*").replace(HOST_STRING, ""))).willReturn(
            aResponse().withStatus(200).withBody(IOUtils.toString(checklistRefactorInput))));
        stubFor(get(urlMatching((checklistNotificationsUri.toString() + ".*empty.*").replace(HOST_STRING, "")))
                .willReturn(aResponse().withStatus(200).withBody(IOUtils.toString(checklistRefactorNoEntryInput))));

    }


    @Test
    public void testExecute()
            throws IOException, JobExecutionException, IllegalArgumentException, FeedException {
        boolean started = !HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().isActive();
        if (started) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().begin();
        }
        try {
            List<Notification> list = dao.find(processedRO, null, null, null, null);
            for (Notification n : list) {
                dao.delete(n);
            }
            StabilityFeedAggregationJob job = new StabilityFeedAggregationJob();
            StabilityFeedAggregationJobListener listener = new StabilityFeedAggregationJobListener();
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(StabilityFeedAggregationJob.RESEARCH_OBJECT_URI, RO_URI);
            Mockito.when(context.getMergedJobDataMap()).thenReturn(jobDataMap);
            SyndFeedInput input = new SyndFeedInput();
            InputStream checklistRefactorInput = StabilityFeedAggregationJobTest.class.getClassLoader()
                    .getResourceAsStream("monitoring/stability_service_notification.xml");
            SyndFeed feed = input.build(new XmlReader(checklistRefactorInput));
            Mockito.when(context.getResult()).thenReturn(feed);
            job.execute(context);
            listener.jobWasExecuted(context, Mockito.mock(JobExecutionException.class));
            list = dao.find(processedRO, null, null, null, null);
            Assert.assertEquals(3, list.size());
            //
        } finally {
            if (started) {
                HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
            }
        }
    }


    @Test
    public void testEmtpyAnswear()
            throws IllegalArgumentException, FeedException, IOException {
        boolean started = !HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().isActive();
        if (started) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().begin();
        }
        try {
            int startsize = dao.find(null, null, null, null, null).size();
            SyndFeedInput input = new SyndFeedInput();
            StabilityFeedAggregationJobListener listener = new StabilityFeedAggregationJobListener();
            InputStream checklistRefactorInput = StabilityFeedAggregationJobTest.class.getClassLoader()
                    .getResourceAsStream("monitoring/stability_service_notification_case_empty.xml");
            SyndFeed feed = input.build(new XmlReader(checklistRefactorInput));
            Mockito.when(context.getResult()).thenReturn(feed);
            listener.jobWasExecuted(context, Mockito.mock(JobExecutionException.class));
            Assert.assertEquals(startsize, dao.find(null, null, null, null, null).size());
        } finally {
            if (started) {
                HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
            }
        }
    }
}
