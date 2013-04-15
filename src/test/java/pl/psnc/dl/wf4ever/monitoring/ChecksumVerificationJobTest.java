package pl.psnc.dl.wf4ever.monitoring;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.monitoring.ChecksumVerificationJob.Result;

public class ChecksumVerificationJobTest extends BaseTest {

    @Before
    public void setUp()
            throws Exception {
        super.setUp();
    }


    @After
    public void tearDown()
            throws Exception {
        super.tearDown();
    }


    @Test
    public final void testExecuteWithNoMismatches()
            throws JobExecutionException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(ChecksumVerificationJob.RESEARCH_OBJECT_URI, researchObject.getUri());
        JobExecutionContext context = Mockito.mock(JobExecutionContext.class);
        Mockito.when(context.getMergedJobDataMap()).thenReturn(jobDataMap);
        ChecksumVerificationJob job = new ChecksumVerificationJob();
        job.setBuilder(builder);
        job.execute(context);
        Assert.assertNotNull(jobDataMap.get(ChecksumVerificationJob.RESULT));
        Result result = (Result) jobDataMap.get(ChecksumVerificationJob.RESULT);
        Assert.assertTrue(result.matches());
        Assert.assertTrue(result.getMismatches().isEmpty());
    }
}
