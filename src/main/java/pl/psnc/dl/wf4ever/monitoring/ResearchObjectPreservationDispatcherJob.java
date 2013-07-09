package pl.psnc.dl.wf4ever.monitoring;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import pl.psnc.dl.wf4ever.darceo.client.DArceoException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import se.kb.oai.OAIException;

/**
 * This job gets a list of all research objects and for each them schedules monitoring jobs.
 * 
 * @author piotrekhol
 * 
 */
public class ResearchObjectPreservationDispatcherJob implements Job {

    /** Resource model builder. */
    private Builder builder;

    /** Id of checksum verification job group. */
    static final String PRESERVATION_GROUP_NAME = "preservationIdentification";
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(ResearchObjectPreservationDispatcherJob.class);


    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        try {
            Synchronization.dArceo();
        } catch (DArceoException | IOException | OAIException e) {
            LOGGER.error("Can't synchronize preservation records with darceo", e);
        }
        if (builder == null) {
            //FIXME RODL URI should be better
            UserMetadata userMetadata = new UserMetadata("rodl", "RODL decay monitor", Role.ADMIN, URI.create("rodl"));
            builder = new Builder(userMetadata);
        }
        try {
            context.getScheduler().getListenerManager()
                    .addJobListener(newPreservationJobListener(), jobGroupEquals(PRESERVATION_GROUP_NAME));
        } catch (SchedulerException e) {
            throw new JobExecutionException("Can't add a checksum job listener", e);
        }

        Set<ResearchObject> researchObjects = ResearchObject.getAll(builder, null);
        for (ResearchObject researchObject : researchObjects) {
            Trigger trigger = newTrigger().withIdentity("Trigger for " + researchObject, "triggers")
                    .withSchedule(simpleSchedule()).build();
            // in here we can add more jobs
            JobDetail job = newPreservationJob(researchObject);
            try {
                context.getScheduler().scheduleJob(job, trigger);
            } catch (SchedulerException e) {
                throw new JobExecutionException(e);
            }

        }
    }


    /**
     * Create a new preservation job. Override to modify.
     * 
     * @param researchObject
     *            a research object that should be preserved
     * @return a new {@link JobDetail} in the group with key PRESERVATION_GROUP_NAME
     */
    protected JobDetail newPreservationJob(ResearchObject researchObject) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(PreservationJob.RESEARCH_OBJECT_URI, researchObject.getUri());
        JobDetail job = newJob(PreservationJob.class)
                .withIdentity("Preservation jon for " + researchObject, PRESERVATION_GROUP_NAME)
                .usingJobData(jobDataMap).build();
        return job;
    }


    /**
     * Create a new listener for checksum verification checking. Override to change the default behaviour.
     * 
     * @return a new {@link ChecksumVerificationJobListener}
     */
    protected JobListener newPreservationJobListener() {
        return new PreservationJobListener();
    }


    public Builder getBuilder() {
        return builder;
    }


    public void setBuilder(Builder builder) {
        this.builder = builder;
    }
}
