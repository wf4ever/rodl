package pl.psnc.dl.wf4ever.monitoring;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import pl.psnc.dl.wf4ever.model.BaseTest;

/**
 * Test that the dispatcher job schedules new jobs for all ROs.
 * 
 * @author piotrekhol
 * 
 */
public class ResearchObjectMonitoringDispatcherJobTest extends BaseTest {

    /** Context with that returns a mock scheduler. */
    private JobExecutionContext context;

    /** Jobs scheduled in the last call. */
    private ScheduledJobsAnswer answer;


    @Override
    @Before
    public void setUp()
            throws Exception {
        super.setUp();

        answer = new ScheduledJobsAnswer();
        Scheduler mockScheduler = mock(Scheduler.class);
        doAnswer(answer).when(mockScheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
        doReturn(mock(ListenerManager.class)).when(mockScheduler).getListenerManager();

        context = mock(JobExecutionContext.class);
        when(context.getScheduler()).thenReturn(mockScheduler);
    }


    /**
     * Test that 2 ROs that exist in the test dataset are scheduled for checksum checking.
     * 
     * @throws JobExecutionException
     *             any problem when running the job
     */
    @Test
    public final void testExecute()
            throws JobExecutionException {
        ResearchObjectMonitoringDispatcherJob job = new ResearchObjectMonitoringDispatcherJob();
        job.setBuilder(builder);
        job.execute(context);
        Assert.assertEquals(4, answer.getJobs().size());
        Set<URI> rosExpected = new HashSet<>();
        rosExpected.add(researchObject.getUri());
        rosExpected.add(researchObject2.getUri());
        Set<URI> rosScheduled = new HashSet<>();
        for (JobDetail jobDetail : answer.getJobs().keySet()) {
            rosScheduled.add((URI) jobDetail.getJobDataMap().get(ChecksumVerificationJob.RESEARCH_OBJECT_URI));
        }
        Assert.assertTrue(rosScheduled.contains(researchObject.getUri()));
        Assert.assertTrue(rosScheduled.contains(researchObject2.getUri()));
    }
}
