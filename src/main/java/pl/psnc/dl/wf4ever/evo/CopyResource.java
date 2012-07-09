package pl.psnc.dl.wf4ever.evo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.BadRequestException;
import pl.psnc.dl.wf4ever.Constants;

/**
 * 
 * @author piotrhol
 * 
 */
@Path("evo/copy/")
public class CopyResource implements JobsContainer {

    @SuppressWarnings("unused")
    private final static Logger logger = Logger.getLogger(CopyResource.class);

    /** Maximum number of concurrent jobs. */
    public static final int MAX_JOBS = 100;

    /** Maximum number of finished jobs kept in memory. */
    public static final int MAX_JOBS_DONE = 100000;

    /** Context. */
    @Context
    private HttpServletRequest request;

    /** URI info. */
    @Context
    private UriInfo uriInfo;

    /** Running jobs. */
    private static Map<UUID, Job> jobs = new ConcurrentHashMap<>(MAX_JOBS);

    /** Statuses of finished jobs. */
    @SuppressWarnings("serial")
    private static Map<UUID, JobStatus> finishedJobs = Collections
            .synchronizedMap(new LinkedHashMap<UUID, JobStatus>() {

                protected boolean removeEldestEntry(Map.Entry<UUID, JobStatus> eldest) {
                    return size() > MAX_JOBS_DONE;
                };
            });


    /**
     * Creates a copy of a research object.
     * 
     * @param copy
     *            operation parameters
     * @return 201 Created
     * @throws BadRequestException
     *             if the operation parameters are incorrect
     */
    @POST
    @Consumes(Constants.RO_COPY_MIME_TYPE)
    public Response createCopyJob(JobStatus status)
            throws BadRequestException {
        if (status.getCopyfrom() == null) {
            throw new BadRequestException("incorrect or missing \"copyfrom\" attribute");
        }
        if (status.getType() == null) {
            throw new BadRequestException("incorrect or missing \"type\" attribute");
        }
        String id = request.getHeader(Constants.SLUG_HEADER);
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        CopyOperation copy = new CopyOperation(id);

        UUID jobUUID = UUID.randomUUID();
        Job job;
        if (!status.isFinalize()) {
            job = new Job(jobUUID, status, this, copy);
        } else {
            FinalizeOperation finalize = new FinalizeOperation();
            job = new Job(jobUUID, status, this, copy, finalize);
        }
        jobs.put(jobUUID, job);

        return Response.created(uriInfo.getAbsolutePath().resolve(jobUUID.toString())).build();
    }


    @Override
    public void onJobDone(Job job) {
        finishedJobs.put(job.getUUID(), job.getStatus());
        jobs.remove(job.getUUID());
    }
}
