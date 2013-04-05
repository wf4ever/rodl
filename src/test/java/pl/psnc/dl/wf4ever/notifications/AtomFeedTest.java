package pl.psnc.dl.wf4ever.notifications;

import junit.framework.Assert;

import org.junit.Test;

import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Person;

public class AtomFeedTest {

    @Test
    public void testCreate() {
        String title = "The Feed Title";
        String id = "The Feed Id";
        Feed feed = AtomFeed.createNewFeed("The Feed Title", "The Feed Id");
        Assert.assertEquals(title, feed.getTitle());
        Assert.assertEquals(id, feed.getId());
        Person author = (Person) feed.getAuthors().get(0);
        Assert.assertEquals(AtomFeed.AUTHOR_EMAIL, author.getEmail());
        Assert.assertEquals(AtomFeed.AUTHOR_NAME, author.getName());
        Assert.assertEquals(AtomFeed.FEED_TYPE, feed.getFeedType());
    }
}
