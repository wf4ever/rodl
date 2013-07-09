package pl.psnc.dl.wf4ever.monitoring;

import java.net.URI;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

import pl.psnc.dl.wf4ever.db.dao.AtomFeedEntryDAO;
import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
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
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(StabilityFeedAggregationJobListener.class);


    @Override
    public String getName() {
        return "Stability job listener";
    }


    /**
     * Constructor.
     */
    public StabilityFeedAggregationJobListener() {
        super();
    }


    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {

        boolean started = !HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().isActive();
        if (started) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().begin();
        }
        try {
            SyndFeed feed = (SyndFeed) context.getResult();
            if (feed != null) {
                for (Object ob : feed.getEntries()) {
                    SyndEntry entry = (SyndEntry) ob;
                    Notification notification = Notification.fromEntry(entry);
                    if (notification.getSource() != null && notification.getCreated() != null) {
                        dao.save(Notification.fromEntry(entry));
                    } else {
                        LOGGER.debug("Can't create a notification " + notification.getTitle());
                    }
                }
            }
        } finally {
            if (started) {
                HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
            }
        }
    }
}
