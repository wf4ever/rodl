package pl.psnc.dl.wf4ever.notifications;

import java.net.URI;
import java.util.Collections;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.notifications.NotificationFeed.FeedType;

import com.sun.syndication.feed.atom.Feed;

/**
 * Tests of the notification feed.
 * 
 * @author piotrekhol
 * 
 */
public class NotificationFeedTest {

    /** A sample notification feed. */
    private NotificationFeed notificationFeed;

    /** Sample notification creation date. */
    private DateTime now = DateTime.now();


    /**
     * Prepare tests.
     */
    @Before
    public void setUp() {
        Notification notification = new Notification();
        notificationFeed = new NotificationFeed.Builder("id").authorName("name").authorEmail("name@example.org")
                .title("title").updated(now).entries(Collections.singletonList(notification)).build();
    }


    /**
     * Test that a correct Atom feed can be generated.
     */
    @Test
    public final void testAsFeed() {
        Feed feed = notificationFeed.asFeed(FeedType.ATOM_1_0, URI.create("http://example.org/feeds/feed.xml?id=4"));
        Assert.assertEquals("atom_1.0", feed.getFeedType());
        Assert.assertEquals("id", feed.getId());
        Assert.assertEquals("title", feed.getTitle());
        Assert.assertEquals(now.toDate(), feed.getUpdated());
        Assert.assertEquals(1, feed.getEntries().size());
        Assert.assertNotNull(feed.getInfo());
    }
}
