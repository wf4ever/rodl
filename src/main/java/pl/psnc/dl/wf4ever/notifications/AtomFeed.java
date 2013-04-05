package pl.psnc.dl.wf4ever.notifications;

import java.util.Collections;

import org.joda.time.DateTime;

import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Person;

/**
 * Feeds factory.
 * 
 * @author pejot
 * 
 */
public class AtomFeed {

    /** Administrator email. */
    public static final String AUTHOR_EMAIL = "rodl@wf4ever.org";
    /** Administrator name. */
    public static final String AUTHOR_NAME = "My name is rodl :)";
    /** Feed type. */
    public static final String FEED_TYPE = "atom_1.0";


    /**
     * Hidden constructor.
     */
    protected AtomFeed() {
        // nope
    }


    /**
     * Create a new feed.
     * 
     * @param title
     *            Feed title.
     * @param id
     *            Feed id.
     * @return a new feed
     */
    static Feed createNewFeed(String title, String id) {
        Feed feed = new Feed();
        feed.setFeedType(FEED_TYPE);
        feed.setId(id.toString());
        feed.setTitle(title);
        feed.setUpdated(DateTime.now().toDate());
        Person author = new Person();
        author.setEmail(AUTHOR_EMAIL);
        author.setName(AUTHOR_NAME);
        feed.setAuthors(Collections.singletonList(author));
        return feed;
    }
}
