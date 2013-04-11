package pl.psnc.dl.wf4ever.monitoring;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * This job calculates checksums for all resources of a research object and compares them with the checksums stored in
 * the database. The result is stored in the context.
 * 
 * @author piotrekhol
 * 
 */
public class ChecksumVerificationJob implements Job {

    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        // TODO Auto-generated method stub

    }

}
