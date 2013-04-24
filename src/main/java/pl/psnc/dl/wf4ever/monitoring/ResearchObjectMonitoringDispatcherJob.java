package pl.psnc.dl.wf4ever.monitoring;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.net.URI;
import java.util.Set;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

/**
 * This job gets a list of all research objects and for each them schedules monitoring jobs.
 * 
 * @author piotrekhol
 * 
 */
public class ResearchObjectMonitoringDispatcherJob implements Job {

    /** Resource model builder. */
    private Builder builder;


    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        if (builder == null) {
            //FIXME RODL URI should be better
            UserMetadata userMetadata = new UserMetadata("rodl", "RODL decay monitor", Role.ADMIN, URI.create("rodl"));
            builder = new Builder(userMetadata);
        }
        Set<ResearchObject> researchObjects = ResearchObject.getAll(builder, null);
        for (ResearchObject researchObject : researchObjects) {
            Trigger trigger = newTrigger().withIdentity("Trigger for " + researchObject, "triggers")
                    .withSchedule(simpleSchedule()).build();
            // in here we can add more jobs
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(ChecksumVerificationJob.RESEARCH_OBJECT_URI, researchObject.getUri());
            JobDetail job = newJob(ChecksumVerificationJob.class)
                    .withIdentity("ChecksumIdentification for " + researchObject, "checksumIdentification")
                    .usingJobData(jobDataMap).build();
            try {
                context.getScheduler().scheduleJob(job, trigger);
            } catch (SchedulerException e) {
                throw new JobExecutionException(e);
            }
        }
    }


    public Builder getBuilder() {
        return builder;
    }


    public void setBuilder(Builder builder) {
        this.builder = builder;
    }
}
