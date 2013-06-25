package pl.psnc.dl.wf4ever.monitoring;

import java.net.URI;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
import pl.psnc.dl.wf4ever.model.Builder;

/**
 * This job gets a list of all research objects and for each them schedules monitoring jobs.
 * 
 * @author piotrekhol
 * 
 */
public class StabilityFeedAggregationJob implements Job {

    /** Resource model builder. */
    private Builder builder;

    /** Id of checksum verification job group. */
    static final String CHECKSUM_CHECKING_GROUP_NAME = "checksumIdentification";


    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        // maybe delete that
        if (builder == null) {
            //FIXME RODL URI should be better
            UserMetadata userMetadata = new UserMetadata("rodl", "RODL decay monitor", Role.ADMIN, URI.create("rodl"));
            builder = new Builder(userMetadata);
        }
        //TODO connect to stability service
    }


    /**
     * Create a new listener. Override to change the default behaviour.
     * 
     * @return a new {@link ChecksumVerificationJobListener}
     */
    protected JobListener newChecksumVerificationJobListener() {
        return new ChecksumVerificationJobListener();
    }


    public Builder getBuilder() {
        return builder;
    }


    public void setBuilder(Builder builder) {
        this.builder = builder;
    }
}
