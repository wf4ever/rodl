package pl.psnc.dl.wf4ever.monitoring;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

import pl.psnc.dl.wf4ever.ApplicationProperties;
import pl.psnc.dl.wf4ever.db.dao.AtomFeedEntryDAO;
import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
import pl.psnc.dl.wf4ever.monitoring.ChecksumVerificationJob.Result;
import pl.psnc.dl.wf4ever.notifications.Notification;
import pl.psnc.dl.wf4ever.notifications.Notification.Summary;
import pl.psnc.dl.wf4ever.notifications.Notification.Title;

/**
 * A listener creating notifications when checksum check job detects mismatches.
 * 
 * @author piotrekhol
 * 
 */
public class ChecksumVerificationJobListener extends JobListenerSupport {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(ChecksumVerificationJobListener.class);


    @Override
    public String getName() {
        return "Checksum verification job listener";
    }


    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        Result result = (Result) context.getResult();
        if (result != null && !result.matches()) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().begin();
            String source = ApplicationProperties.getContextPath() != null ? ApplicationProperties.getContextPath()
                    : "/";
            Notification notification = new Notification.Builder(result.getResearchObject().getUri())
                    .title(Title.checksumMismatch(result.getResearchObject()))
                    .summary(Summary.checksumMismatch(result.getResearchObject(), result.getMismatches()))
                    .source(source).sourceName("RODL").build();
            AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
            dao.save(notification);
            LOGGER.debug("Generated a notification about a checksum mismatch for RO: " + result.getResearchObject());
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }
    }
}
