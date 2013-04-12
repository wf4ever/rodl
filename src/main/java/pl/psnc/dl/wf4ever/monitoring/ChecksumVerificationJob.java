package pl.psnc.dl.wf4ever.monitoring;

import java.net.URI;

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

    /** Key for the input data. The value must be a URI. */
    public static final String RESEARCH_OBJECT_URI = "ResearchObjectUri";


    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        URI researchObjectUri = (URI) context.getMergedJobDataMap().get(RESEARCH_OBJECT_URI);
        // TODO Auto-generated method stub

    }
}
