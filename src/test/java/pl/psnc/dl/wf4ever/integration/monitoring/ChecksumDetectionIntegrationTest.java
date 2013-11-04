package pl.psnc.dl.wf4ever.integration.monitoring;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import pl.psnc.dl.wf4ever.dl.DigitalLibrary;
import pl.psnc.dl.wf4ever.integration.AbstractIntegrationTest;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;
import pl.psnc.dl.wf4ever.monitoring.StabilityFeedAggregationJobTest;
import pl.psnc.dl.wf4ever.storage.FilesystemDLFactory;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;

/**
 * Check that checksum mismatches are reported as notifications.
 * 
 * @author piotrekhol
 * 
 */
@Category(IntegrationTest.class)
@Ignore
public class ChecksumDetectionIntegrationTest extends AbstractIntegrationTest {

	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(ChecksumDetectionIntegrationTest.class);

	/** A test HTTP mock server. */
	@Rule
	public final WireMockRule WIREMOCK_RULE = new WireMockRule(8089); // No-args
																		// constructor
																		// defaults
																		// to
																		// port
																		// 8080

	/** A sample file name. */
	private String filePath = "foo.txt";

	private URI ro;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		ro = createRO();
		addLoremIpsumFile(ro, filePath);

		InputStream checklistRefactorNoEntryInput = StabilityFeedAggregationJobTest.class
				.getClassLoader()
				.getResourceAsStream("monitoring/stability_service_notification_case_empty.xml");
		stubFor(get(urlMatching((".*roevaluate.*"))).willReturn(
				aResponse().withStatus(200).withBody(
						IOUtils.toString(checklistRefactorNoEntryInput))));
	}

	/**
	 * Modify a checksum in the database, force monitoring and check that a
	 * notification is generated.
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
	public final void test() throws FeedException, IOException, InterruptedException {

		// check what is the most recent notification
		SyndFeed feed = getNotifications(ro, null);
		List<SyndEntry> entries = feed.getEntries();
		// modify the file
		String checksum2 = modifyFile(filePath);
		if (checksum2 == null) {
			LOGGER.warn("Can't modify a file, skipping the test");
			return;
		}
		// sleep for a second because of WFE-1049
		Thread.sleep(1000);
		// force monitoring
		webResource.path("admin/monitor/all").header("Authorization", "Bearer " + accessToken)
				.post();
		// now wait for the notification to appear
		int seconds = 0;
		DateTime from = entries.isEmpty() ? null : new DateTime(entries.get(entries.size() - 1)
				.getPublishedDate()).plusSeconds(1);
		while (++seconds < 10) {
			feed = getNotifications(ro, from);
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
		Assert.assertTrue(entry.getTitle(),
				entry.getTitle().matches("Research Object .+ has become corrupt!"));
		Assert.assertTrue(
				entry.getDescription().getValue() + "/" + checksum2,
				entry.getDescription()
						.getValue()
						.matches(
								".*File .*" + filePath + ": expected checksum .+, found "
										+ checksum2 + ".*"));
	}

	/**
	 * Create a temporary FilesystemDL, turn off updating the resource stats and
	 * modify the file.
	 * 
	 * @param filePath2
	 *            file path
	 * @return the new file's checksum
	 * @throws IOException
	 *             when accessing the test files throws it
	 */
	private String modifyFile(String filePath2) throws IOException {
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(
				"connection.properties")) {
			Properties properties = new Properties();
			properties.load(is);
			if ("true".equals(properties.getProperty("dlibra", "false"))) {
				return null;
			}
			DigitalLibrary dl = Mockito
					.spy(new FilesystemDLFactory(properties).getDigitalLibrary());
			// don't update the db
			Mockito.doReturn(null).when(dl).updateFileInfo(ro, filePath2, "text/plain");
			String text = "lorem ipsum modified";
			dl.createOrUpdateFile(ro, filePath2, IOUtils.toInputStream(text), "text/plain");
			return DigestUtils.md5Hex(IOUtils.toInputStream(text));
		}
	}

}
