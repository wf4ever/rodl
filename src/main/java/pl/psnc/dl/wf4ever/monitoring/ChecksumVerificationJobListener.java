package pl.psnc.dl.wf4ever.monitoring;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

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
        // TODO Auto-generated method stub
        super.jobWasExecuted(context, jobException);
    }

}
