package pl.psnc.dl.wf4ever.monitoring;

import java.net.URI;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

import pl.psnc.dl.wf4ever.db.dao.AtomFeedEntryDAO;
import pl.psnc.dl.wf4ever.notifications.Notification;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * A listener which create notifications in db once the StabilityFeedAggregationJob aggregated something.
 * 
 * @author pejot
 * 
 */
public class StabilityFeedAggregationJobListener extends JobListenerSupport {

    /** Dao object. */
    AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
    /** Service Uri. */
    URI checklistNotificationsUri;


    @Override
    public String getName() {
        //TODO
        return "Checksum verification job listener";
    }


    /**
     * Constructor.
     */
    public StabilityFeedAggregationJobListener() {
        super();
    }


    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        SyndFeed feed = (SyndFeed) context.getResult();
        if (feed != null) {
            for (Object ob : feed.getEntries()) {
                SyndEntry entry = (SyndEntry) ob;
                dao.save(Notification.fromEntry(entry));
            }
        }
    }
}
