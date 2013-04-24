package pl.psnc.dl.wf4ever.monitoring;

import java.util.LinkedHashMap;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.quartz.JobDetail;
import org.quartz.Trigger;

/**
 * An implementation of Mockito's Answer class that sets the job result.
 * 
 * @author piotrekhol
 * 
 */
final class ScheduledJobsAnswer implements Answer<Void> {

    /** The job result. */
    private final LinkedHashMap<JobDetail, Trigger> jobs = new LinkedHashMap<>();


    @Override
    public Void answer(InvocationOnMock invocation)
            throws Throwable {
        jobs.put((JobDetail) invocation.getArguments()[0], (Trigger) invocation.getArguments()[1]);
        return null;
    }


    public LinkedHashMap<JobDetail, Trigger> getJobs() {
        return jobs;
    }

}
