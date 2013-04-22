package pl.psnc.dl.wf4ever.notifications;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Person;

/**
 * Feeds factory.
 * 
 * @author pejot
 * @author piotrekhol
 * 
 */
public final class AtomFeed {

    /**
     * Feed type for serialization.
     * 
     * @author piotrekhol
     * 
     */
    public enum FeedType {
        /** Atom 1.0. */
        ATOM_1_0("atom_1.0");

        /** Sun's String value. */
        private String value;


        /**
         * Internal constructor.
         * 
         * @param value
         *            Sun's String value
         */
        FeedType(String value) {
            this.value = value;
        }
    }


    /** Feed ID. */
    private String id;

    /** Feed title. */
    private String title;

    /** Administrator email. */
    private String authorEmail;

    /** Administrator name. */
    private String authorName;

    /** Feed type. */
    private FeedType feedType;

    /** Update date. */
    private DateTime updated;

    /** Feed entries. */
    private List<AtomFeedEntry> entries = new ArrayList<>();


    /**
     * Internal constructor used only by builder.
     * 
     * @param builder
     *            a builder instance with fields to copy
     */
    private AtomFeed(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.authorEmail = builder.authorEmail;
        this.authorName = builder.authorName;
        this.feedType = builder.feedType;
        this.updated = builder.updated;
        this.entries = builder.entries;
    }


    public List<AtomFeedEntry> getEntries() {
        return entries;
    }


    public void setEntries(List<AtomFeedEntry> entries) {
        this.entries = entries;
    }


    /**
     * Create a new Sun feed.
     * 
     * @return this feed in another format
     */
    public Feed asSunFeed() {
        Feed feed = new Feed();
        feed.setFeedType(feedType.value);
        feed.setId(id);
        feed.setTitle(title);
        feed.setUpdated(updated.toDate());
        Person author = new Person();
        author.setEmail(authorEmail);
        author.setName(authorName);
        feed.setAuthors(Collections.singletonList(author));
        List<Entry> entries2 = new ArrayList<>();
        for (AtomFeedEntry entry : this.entries) {
            entries2.add(entry.asSunFeedEntry());
        }
        feed.setEntries(entries2);
        return feed;
    }


    /**
     * A builder for creating the {@link AtomFeed} using the builder design pattern.
     * 
     * @author piotrekhol
     * 
     */
    static class Builder {

        /** Feed ID. */
        private String id;

        /** Feed title. */
        private String title;

        /** Administrator email. */
        private String authorEmail = "rodl@wf4ever.org";

        /** Administrator name. */
        private String authorName = "My name is rodl :)";

        /** Feed type. */
        private FeedType feedType = FeedType.ATOM_1_0;

        /** Update date. */
        private DateTime updated = DateTime.now();

        /** Feed entries. */
        private List<AtomFeedEntry> entries = new ArrayList<>();


        /**
         * A constructor.
         * 
         * @param id
         *            feed ID
         */
        public Builder(String id) {
            this.id = id;
        }


        /**
         * Create an {@link AtomFeed} instance based on this builder.
         * 
         * @return a new feed instance
         */
        public AtomFeed build() {
            return new AtomFeed(this);
        }


        /**
         * Feed title.
         * 
         * @param title
         *            feed title
         * @return this builder
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }


        /**
         * Author email.
         * 
         * @param email
         *            Author email
         * @return this builder
         */
        public Builder authorEmail(String email) {
            this.authorEmail = email;
            return this;
        }


        /**
         * Author name.
         * 
         * @param name
         *            Author name
         * @return this builder
         */
        public Builder authorName(String name) {
            this.authorName = name;
            return this;
        }


        /**
         * Feed type.
         * 
         * @param feedType
         *            feed type
         * @return this builder
         */
        public Builder feedType(FeedType feedType) {
            this.feedType = feedType;
            return this;
        }


        /**
         * Update timestamp.
         * 
         * @param updated
         *            Update timestamp
         * @return this builder
         */
        public Builder updated(DateTime updated) {
            this.updated = updated;
            return this;
        }


        /**
         * Feed entries.
         * 
         * @param entries
         *            feed entries
         * @return this builder
         */
        public Builder entries(List<AtomFeedEntry> entries) {
            this.entries = entries;
            return this;
        }

    }


    /**
     * Commonly used titles.
     * 
     * @author piotrekhol
     * 
     */
    public static class Title {

        /**
         * Get a title based on the query params.
         * 
         * @param roUri
         *            research object URI
         * @param from
         *            filter param - date from
         * @param to
         *            filter param date to
         * @return a feed title
         */
        public static String build(URI roUri, Date from, Date to) {
            String result = "Notifications for ";
            if (roUri == null) {
                result += "all ROs";
            } else {
                result += roUri.toString();
            }
            if (from != null || to != null) {
                result += "\nRange:\n";
                if (from != null) {
                    result += "\nfrom: " + from.toString();
                }
                if (to != null) {
                    result += "\nto: " + to.toString();
                }
            }
            return result;
        }

    }

}
