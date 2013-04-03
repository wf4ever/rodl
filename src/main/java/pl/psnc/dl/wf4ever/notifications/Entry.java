package pl.psnc.dl.wf4ever.notifications;

import java.net.URI;

import org.joda.time.DateTime;

//@TODO conenct to the hibernate
/**
 * Represents a simple entry of the feed, contains a simple report of the event like update, damage and so on.
 * 
 * @author pejot
 */
public class Entry {

    /** Creation date. */
    DateTime crated;
    /** Title. */
    String title;
    /** The event source/discover. */
    URI source;
    /** Id. */
    Integer id;
    /** Summary/Description(Content). */
    String summary;
}
