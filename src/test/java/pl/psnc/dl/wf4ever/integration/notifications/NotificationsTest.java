package pl.psnc.dl.wf4ever.integration.notifications;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.integration.AbstractIntegrationTest;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;

import com.hp.hpl.jena.vocabulary.DCTerms;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

@Category(IntegrationTest.class)
@Ignore
public class NotificationsTest extends AbstractIntegrationTest {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// TODO get the resource path from service description
		// webResource.path("notifications/").header("Authorization", "Bearer "
		// + adminCreds).delete(String.class);
	}

	@Test
	public void testDelete() {
		URI ro = createRO();
		String before = (webResource.path("notifications/").get(String.class));
		webResource.path("notifications/").header("Authorization", "Bearer " + accessToken)
				.delete(String.class);
		String after = (webResource.path("notifications/").get(String.class));
		Assert.assertTrue(before.contains("entry"));
		Assert.assertFalse(after.contains("entry"));
	}

	@Test
	public void testFeed() {
		String resultAll = (webResource.path("notifications/").get(String.class));
	}

	@Test
	public void testNotificationsCreateNoFilters() {
		URI ro = createRO();
		String resultAll = (webResource.path("notifications/").get(String.class));
		Assert.assertTrue(resultAll.contains(ro.toString()));
		Assert.assertTrue(resultAll.contains("urn:X-rodl:"));
		Assert.assertTrue(resultAll.contains("Research Object has been created"));
		Assert.assertTrue(resultAll
				.contains("&amp;lt;p&amp;gt;A new Research Object has been created.&amp;lt;/p&amp;gt;"));
		Assert.assertTrue(resultAll.contains("http://purl.org/dc/terms/source"));
		Assert.assertTrue(resultAll.contains("http://www.openarchives.org/ore/terms/describes"));
		Assert.assertTrue(resultAll.contains("Notifications for all ROs"));
	}

	@Test
	public void testNotificationsFilterRO() {
		URI ro = createRO();
		URI ro2 = createRO();
		String resultAll = (webResource.path("notifications/").queryParam("ro", ro.toString())
				.get(String.class));
		Assert.assertTrue(resultAll.contains("Notifications for " + ro.toString()));
		Assert.assertFalse(resultAll.contains("Notifications for " + ro2.toString()));
	}

	@Test
	public void testNotificationSource() throws IllegalArgumentException, MalformedURLException,
			FeedException, IOException, URISyntaxException {
		URI ro = createRO();
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(new XmlReader(webResource.path("notifications/")
				.queryParam("ro", ro.toString()).getURI().toURL()));
		for (Object object : feed.getEntries()) {
			SyndEntry entry = (SyndEntry) object;
			for (Object obLink : entry.getLinks()) {
				SyndLink link = (SyndLink) obLink;
				if (link != null && link.getRel().equals(DCTerms.source.toString())) {
					// the source is this producer host
					Assert.assertTrue(link.getHref().contains(ro.getHost()));
				}
			}
		}
	}

	@Test
	public void testRanges() throws InterruptedException {
		DateTime start = DateTime.now();
		Thread.sleep(2000);
		URI ro = createRO();
		Thread.sleep(2000);
		DateTime middle = DateTime.now();
		Thread.sleep(2000);
		URI ro2 = createRO();
		Thread.sleep(2000);
		URI ro3 = createRO();
		Thread.sleep(2000);
		DateTime end = DateTime.now();

		String beforeStart = webResource.path("notifications/").queryParam("to", start.toString())
				.get(String.class);
		String afterStart = webResource.path("notifications/").queryParam("from", start.toString())
				.get(String.class);
		String afterMiddle = webResource.path("notifications/")
				.queryParam("from", middle.toString()).get(String.class);
		String beforeMiddle = webResource.path("notifications/")
				.queryParam("to", middle.toString()).get(String.class);
		String afterEnd = webResource.path("notifications/").queryParam("from", end.toString())
				.get(String.class);
		String beforeEnd = webResource.path("notifications/").queryParam("to", end.toString())
				.get(String.class);

		Assert.assertFalse(beforeStart.contains(ro.toString()));
		Assert.assertFalse(beforeStart.contains(ro2.toString()));
		Assert.assertFalse(beforeStart.contains(ro3.toString()));

		Assert.assertTrue(afterStart.contains(ro.toString()));
		Assert.assertTrue(afterStart.contains(ro2.toString()));
		Assert.assertTrue(afterStart.contains(ro3.toString()));

		Assert.assertTrue(beforeMiddle.contains(ro.toString()));
		Assert.assertFalse(beforeMiddle.contains(ro2.toString()));
		Assert.assertFalse(beforeMiddle.contains(ro3.toString()));

		Assert.assertFalse(afterMiddle.contains(ro.toString()));
		Assert.assertTrue(afterMiddle.contains(ro2.toString()));
		Assert.assertTrue(afterMiddle.contains(ro3.toString()));

		Assert.assertTrue(beforeEnd.contains(ro.toString()));
		Assert.assertTrue(beforeEnd.contains(ro2.toString()));
		Assert.assertTrue(beforeEnd.contains(ro3.toString()));

		Assert.assertFalse(afterEnd.contains(ro.toString()));
		Assert.assertFalse(afterEnd.contains(ro2.toString()));
		Assert.assertFalse(afterEnd.contains(ro3.toString()));
	}

	@Test
	public void testNotificationsLimit() {
		URI ro = createRO();
		URI ro2 = createRO();
		String resultAll = (webResource.path("notifications/").queryParam("limit", "1")
				.get(String.class));
		// only one entry
		Assert.assertEquals(2, resultAll.split("<entry>").length);
	}
}
