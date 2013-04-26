package pl.psnc.dl.wf4ever.monitoring;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

import pl.psnc.dl.wf4ever.db.dao.AtomFeedEntryDAO;
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

    @Override
    public String getName() {
        return "Checksum verification job listener";
    }


    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        Result result = (Result) context.getResult();
        if (result != null && !result.matches()) {
            Notification notification = new Notification.Builder(result.getResearchObject().getUri())
                    .title(Title.checksumMismatch(result.getResearchObject()))
                    .summary(Summary.checksumMismatch(result.getResearchObject(), result.getMismatches())).build();
            AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
            dao.save(notification);
        }
    }

}
