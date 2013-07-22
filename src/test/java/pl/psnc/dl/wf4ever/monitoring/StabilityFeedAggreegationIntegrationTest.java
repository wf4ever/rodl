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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.IntegrationTest;
import pl.psnc.dl.wf4ever.W4ETest;
import pl.psnc.dl.wf4ever.db.dao.AtomFeedEntryDAO;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sun.syndication.io.FeedException;

/**
 * Check that checksum mismatches are reported as notifications.
 * 
 * @author piotrekhol
 * 
 */
@Category(IntegrationTest.class)
public class StabilityFeedAggreegationIntegrationTest extends W4ETest {

    /** A sample file name. */
    private String filePath = "foo.txt";
    /** A test HTTP mock server. */
    @Rule
    public final WireMockRule WIREMOCK_RULE = new WireMockRule(8089); // No-args constructor defaults to port 8080
    AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
    URI checklistNotificationsUri;
    protected static final String HOST_STRING = "http://127.0.0.1:8089";


    @Before
    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        createUserWithAnswer(userIdSafe, username).close();
        accessToken = createAccessToken(userId);
        deleteROs();
        ro = createRO(accessToken);
        addFile(ro, filePath, accessToken);
        setUpMockito();
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


    public void setUpMockito()
            throws IOException {
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
    public final void test()
            throws FeedException, IOException, InterruptedException {
        URI testUri = URI.create("http://127.0.0.1:8089/rodl/ROs/SimpleRO/");
        List<?> entriesBefore = getNotifications(testUri, null).getEntries();
        //force scheduler
        webResource.path("admin/monitor/all").header("Authorization", "Bearer " + accessToken).post();
        int times = 0;
        while (++times < 20) {
            Thread.sleep(500);
            List<?> entriesAfter = getNotifications(testUri, null).getEntries();
            if (entriesBefore.size() < entriesAfter.size()) {
                return;
            }
        }
        Assert.fail("The Stability Notification for object " + ro + " wasn't created");
    }
}
