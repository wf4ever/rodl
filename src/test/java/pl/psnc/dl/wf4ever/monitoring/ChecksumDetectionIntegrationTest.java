package pl.psnc.dl.wf4ever.monitoring;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.IntegrationTest;
import pl.psnc.dl.wf4ever.W4ETest;
import pl.psnc.dl.wf4ever.db.ResourceInfo;
import pl.psnc.dl.wf4ever.db.dao.ResourceInfoDAO;
import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
import pl.psnc.dl.wf4ever.vocabulary.NotificationService;

import com.damnhandy.uri.template.UriTemplate;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * Check that checksum mismatches are reported as notifications.
 * 
 * @author piotrekhol
 * 
 */
@Category(IntegrationTest.class)
public class ChecksumDetectionIntegrationTest extends W4ETest {

    /** A test HTTP mock server. */
    @Rule
    public final WireMockRule WIREMOCK_RULE = new WireMockRule(8089); // No-args constructor defaults to port 8080

    /** A sample file name. */
    private String filePath = "foo.txt";


    @Before
    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        createUserWithAnswer(userIdSafe, username).close();
        accessToken = createAccessToken(userId);
        ro = createRO(accessToken);
        addFile(ro, filePath, accessToken);

        InputStream checklistRefactorNoEntryInput = StabilityFeedAggregationJobTest.class.getClassLoader()
                .getResourceAsStream("monitoring/stability_service_notification_case_empty.xml");
        stubFor(get(urlMatching((".*roevaluate.*"))).willReturn(
            aResponse().withStatus(200).withBody(IOUtils.toString(checklistRefactorNoEntryInput))));
    }


    @After
    @Override
    public void tearDown()
            throws Exception {
        deleteROs();
        deleteAccessToken(accessToken);
        deleteUser(userIdSafe);
        super.tearDown();
    }


    /**
     * Modify a checksum in the database, force monitoring and check that a notification is generated.
     * 
     * @throws FeedException
     *             can't load the feed
     * @throws IOException
     *             can't load the feed
     * @throws InterruptedException
     *             interrupted when waiting for the notifications
     */
    @Test
    @SuppressWarnings("unchecked")
    public final void test()
            throws FeedException, IOException, InterruptedException {
        //check what is the most recent notification
        SyndFeed feed = getNotifications(null);
        List<SyndEntry> entries = feed.getEntries();
        //modify the checksums
        ResourceInfoDAO dao = new ResourceInfoDAO();
        List<ResourceInfo> resources = dao.findByPathSufix(filePath);
        for (ResourceInfo resource : resources) {
            // modify the checksum
            resource.setChecksum(resource.getChecksum() + "_modified");
            dao.save(resource);
        }
        HibernateUtil.getSessionFactory().getCurrentSession().flush();
        //sleep for a second because of WFE-1049
        Thread.sleep(1000);
        //force monitoring
        webResource.path("admin/monitor/all").header("Authorization", "Bearer " + accessToken).post();
        //now wait for the notification to appear
        int seconds = 0;
        DateTime from = entries.isEmpty() ? null : new DateTime(entries.get(entries.size() - 1).getPublishedDate())
                .plusSeconds(1);
        while (++seconds < 10) {
            feed = getNotifications(from);
            if (!feed.getEntries().isEmpty()) {
                break;
            }
            System.out.print(".");
            Thread.sleep(1000);
        }
        if (feed.getEntries().isEmpty()) {
            Assert.fail("No notification for 10s");
        }
        // verify that the notification is about the checksum mismatch
        SyndEntry entry = (SyndEntry) feed.getEntries().get(feed.getEntries().size() - 1);
        Assert.assertTrue(entry.getTitle(), entry.getTitle().matches("Research Object .+ has become corrupt!"));
        Assert.assertTrue(
            entry.getDescription().getValue(),
            entry.getDescription()
                    .getValue()
                    .matches(
                        ".*File .*" + filePath + ": expected checksum (?<correct>.+)_modified, found \\k<correct>.*"));
    }


    /**
     * Get notifications.
     * 
     * @param from
     *            optional start point
     * @return a feed
     * @throws FeedException
     *             can't load the feed
     * @throws IOException
     *             can't load the feed
     */
    private SyndFeed getNotifications(DateTime from)
            throws FeedException, IOException {
        Model model = FileManager.get().loadModel(webResource.getURI().toString());
        Resource serviceResource = model.getResource(webResource.getURI().toString());
        String notificationsUriTemplateString = serviceResource.listProperties(NotificationService.notifications)
                .next().getObject().asLiteral().getString();
        UriTemplate uriTemplate = UriTemplate.fromTemplate(notificationsUriTemplateString);
        uriTemplate = uriTemplate.set("ro", ro.toString());
        if (from != null) {
            uriTemplate = uriTemplate.set("from", ISODateTimeFormat.dateTime().print(from));
        }
        URI notificationsUri = UriBuilder.fromUri(uriTemplate.expand()).build();
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(notificationsUri.toURL()));
        return feed;
    }

}
