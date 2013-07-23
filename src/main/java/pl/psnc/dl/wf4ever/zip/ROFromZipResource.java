package pl.psnc.dl.wf4ever.zip;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.auth.RequestAttribute;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.job.Job;
import pl.psnc.dl.wf4ever.job.JobStatus;
import pl.psnc.dl.wf4ever.job.JobsContainer;
import pl.psnc.dl.wf4ever.model.Builder;

import com.sun.jersey.api.NotFoundException;

/**
 * A list of research objects REST APIs.
 * 
 * @author piotrhol
 * 
 */
@Path("ROs/zip/")
public class ROFromZipResource implements JobsContainer {

    /** logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(ROFromZipResource.class);

    /** URI info. */
    @Context
    UriInfo uriInfo;

    /** Resource builder. */
    @RequestAttribute("Builder")
    private Builder builder;

    /** Maximum number of concurrent jobs. */
    public static final int MAX_JOBS = 100;

    /** Maximum number of finished jobs kept in memory. */
    public static final int MAX_JOBS_DONE = 100000;

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
     * Create a new RO based on a ZIP sent in the request.
     * 
     * @param researchObjectId
     *            slug header
     * @param zipStream
     *            ZIP input stream
     * @return 201 Created
     * @throws BadRequestException
     *             the ZIP content is not a valid RO
     */
    @POST
    @Path("store/")
    @Consumes("application/zip")
    public Response storeROFromGivenZip(@HeaderParam("Slug") String researchObjectId, InputStream zipStream)
            throws BadRequestException {
        ArrayList<String> missingParamters = new ArrayList<String>();
        if (zipStream == null) {
            missingParamters.add("Zip file");
        }
        if (researchObjectId == null || researchObjectId.isEmpty()) {
            missingParamters.add("research object id");
        }
        if (!missingParamters.isEmpty()) {
            String errorMessage = "Missing paramters:";
            for (String missingParamter : missingParamters) {
                errorMessage += "\n" + missingParamter;
            }
            throw new BadRequestException(errorMessage);
        }
        UUID jobUUID = UUID.randomUUID();
        JobStatus jobStatus = new ROFromZipJobStatus();
        jobStatus.setTarget(URI.create(researchObjectId));
        StoreROFromGivenZipOperation operation = new StoreROFromGivenZipOperation(builder, zipStream, uriInfo);
        Job job = new Job(jobUUID, jobStatus, this, operation);
        jobs.put(jobUUID, job);
        job.run();
        try {
            Response result = Response.created(uriInfo.getAbsolutePath().resolve(jobUUID.toString()))
                    .entity(job.getStatus()).build();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @POST
    @Path("create/")
    @Consumes("application/zip")
    public Response createROFromGivenZip(@HeaderParam("Slug") String researchObjectId, InputStream zipStream) {
        return null;
    }


    /**
     * Retrieve the job status.
     * 
     * @param uuid
     *            job id
     * @return job status
     */
    @GET
    @Path("{id}")
    public JobStatus getJob(@PathParam("id") UUID uuid) {
        if (jobs.containsKey(uuid)) {
            return jobs.get(uuid).getStatus();
        }
        if (finishedJobs.containsKey(uuid)) {
            return finishedJobs.get(uuid);
        }
        throw new NotFoundException("Job not found: " + uuid);
    }


    /**
     * Abort the job.
     * 
     * @param uuid
     *            job id
     */
    @DELETE
    @Path("{id}")
    public void abortJob(@PathParam("id") UUID uuid) {
        if (jobs.containsKey(uuid)) {
            jobs.get(uuid).abort();
            jobs.remove(uuid);
        }
        if (finishedJobs.containsKey(uuid)) {
            finishedJobs.remove(uuid);
        }
        throw new NotFoundException("Job not found: " + uuid);
    }


    //helpers

    public static Map<UUID, JobStatus> getFinishedJobs() {
        return finishedJobs;
    }


    public static void setFinishedJobs(Map<UUID, JobStatus> finishedJobs) {
        ROFromZipResource.finishedJobs = finishedJobs;
    }


    public static Map<UUID, Job> getJobs() {
        return jobs;
    }


    public static void setJobs(Map<UUID, Job> jobs) {
        ROFromZipResource.jobs = jobs;
    }


    //JobsContainerInterface

    @Override
    public void onJobDone(Job job) {
        finishedJobs.put(job.getUUID(), job.getStatus());
        jobs.remove(job.getUUID());
    }

}
