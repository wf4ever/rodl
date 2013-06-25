package pl.psnc.dl.wf4ever.monitoring;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

import pl.psnc.dl.wf4ever.monitoring.ChecksumVerificationJob.Result;

/**
 * A listener creating notifications when checksum check job detects mismatches.
 * 
 * @author piotrekhol
 * 
 */
public class StabilityFeedAggregationJobListener extends JobListenerSupport {

    @Override
    public String getName() {
        //TODO
        return "Checksum verification job listener";
    }


    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        Result result = (Result) context.getResult();
        if (result != null && !result.matches()) {
            //TODO
        }
    }
}
