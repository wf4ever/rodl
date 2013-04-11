package pl.psnc.dl.wf4ever.monitoring;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;

/**
 * This class manages the jobs and triggers.
 * 
 * @author piotrekhol
 * 
 */
public class RodlScheduler {

    public static void start()
            throws SchedulerException {
        SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();

        Scheduler sched = schedFact.getScheduler();
        sched.start();

        JobDetail job = newJob(ChecksumVerificationJob.class).withIdentity("myJob", "group1").build();

        Trigger trigger = newTrigger().withIdentity("trigger3", "group1")
                .withSchedule(cronSchedule("0 0/2 8-17 * * ?").withMisfireHandlingInstructionIgnoreMisfires())
                .forJob("myJob", "group1").build();
        sched.scheduleJob(job, trigger);
    }
}
