package pl.psnc.dl.wf4ever.rosrs;

import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * A REST API resource corresponding to a zipped RO.
 * 
 * @author Piotr Hołubowicz
 * 
 */
@Path("zippedROs/{ro_id}/")
public class ZippedResearchObjectResource {

    /** URI info. */
    @Context
    UriInfo uriInfo;


    /**
     * Returns zip archive with contents of RO version.
     * 
     * @param researchObjectId
     *            RO identifier - defined by the user
     * @return 200 OK
     * @throws DigitalLibraryException
     *             could not get the RO frol dLibra
     * @throws NotFoundException
     *             could not get the RO frol dLibra
     */
    @GET
    @Produces({ "application/zip", "multipart/related" })
    public Response getZippedRO(@PathParam("ro_id") String researchObjectId)
            throws DigitalLibraryException, NotFoundException {
        URI uri = URI.create(uriInfo.getAbsolutePath().toString().replaceFirst("zippedROs", "ROs"));
        ResearchObject researchObject = ResearchObject.get(uri);
        if (researchObject == null) {
            throw new NotFoundException("Research Object not found");
        }
        InputStream body = ROSRService.DL.get().getZippedResearchObject(researchObject.getUri());
        //TODO add all named graphs from SMS that start with the base URI
        ContentDisposition cd = ContentDisposition.type("application/zip").fileName(researchObjectId + ".zip").build();
        return ResearchObjectResource.addLinkHeaders(Response.ok(body), uriInfo, researchObjectId)
                .header("Content-disposition", cd).build();
    }
}
