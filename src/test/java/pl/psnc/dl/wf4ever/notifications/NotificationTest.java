package pl.psnc.dl.wf4ever.notifications;

import java.net.URI;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.vocabulary.DCTerms;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;

/**
 * Tests of the {@link Notification} class.
 * 
 * @author piotrekhol
 * 
 */
public class NotificationTest {

    /** A sample notification. */
    private Notification notification;

    /** Sample notification creation date. */
    private Date now = DateTime.now().toDate();


    /**
     * Init tests.
     */
    @Before
    public void setUp() {
        notification = new Notification();
        notification.setCreated(now);
        notification.setId(1);
        notification.setSource("foo", "test");
        notification.setSubject("http://example.org/ro1/");
        notification.setSummary("<p>Lorem ipsum</p>");
        notification.setTitle("Test Notification");
    }


    /**
     * Test that a correct Feed Entry is generated.
     */
    @Test
    public void testAsFeedEntry() {
        Entry entry = notification.asFeedEntry(URI.create("http://example.org/feeds/feed.xml?id=3"));
        Assert.assertEquals(now, entry.getCreated());
        Assert.assertEquals("urn:X-rodl:1", entry.getId());
        Assert.assertEquals(Content.HTML, entry.getSummary().getType());
        Assert.assertEquals("&lt;p&gt;Lorem ipsum&lt;/p&gt;", entry.getSummary().getValue());
        Assert.assertEquals("Test Notification", entry.getTitle());

        @SuppressWarnings("unchecked")
        List<Link> links = entry.getOtherLinks();
        Link sourceLink = null;
        for (Link link : links) {
            if (link.getRel().equals(DCTerms.source.getURI())) {
                sourceLink = link;
                break;
            }
        }
        Assert.assertNotNull(sourceLink);
        Assert.assertEquals("http://example.org/feeds/foo", sourceLink.getHref());
        Assert.assertEquals(sourceLink.getTitle(), "test");
    }
}
