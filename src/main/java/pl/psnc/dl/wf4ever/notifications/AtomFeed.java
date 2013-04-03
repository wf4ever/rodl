package pl.psnc.dl.wf4ever.notifications;

import java.util.Collections;

import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.feed.synd.SyndPersonImpl;

/**
 * Feeds factory.
 * 
 * @author pejot
 * 
 */
public class AtomFeed {

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
        feed.setFeedType("atom_1.0");
        feed.setId(id);
        feed.setTitle(title);
        SyndPerson author = new SyndPersonImpl();
        author.setEmail("example@example.org");
        author.setName("My name is rodl ;)");
        feed.setAuthors(Collections.singletonList(author));
        return feed;
    }
}
