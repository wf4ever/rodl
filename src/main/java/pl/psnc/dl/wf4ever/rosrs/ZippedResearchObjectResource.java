package pl.psnc.dl.wf4ever.rosrs;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import javax.naming.OperationNotSupportedException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.auth.SecurityFilter;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dlibra.service.IdNotFoundException;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * 
 * @author Piotr Hołubowicz
 * 
 */
@Path("zippedROs/{ro_id}/")
public class ZippedResearchObjectResource {

    @Context
    HttpServletRequest request;

    @Context
    UriInfo uriInfo;


    /**
     * Returns zip archive with contents of RO version.
     * 
     * @param researchObjectId
     *            RO identifier - defined by the user
     * @return
     * @throws UnknownHostException
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws IOException
     * @throws DigitalLibraryException
     * @throws IdNotFoundException
     * @throws OperationNotSupportedException
     * @throws NotFoundException
     */
    @GET
    @Produces({ "application/zip", "multipart/related" })
    public Response getZippedRO(@PathParam("ro_id") String researchObjectId)
            throws RemoteException, MalformedURLException, UnknownHostException, DigitalLibraryException,
            NotFoundException {
        InputStream body = SecurityFilter.DL.get().getZippedVersion(Constants.workspaceId, researchObjectId,
            Constants.versionId);
        //TODO add all named graphs from SMS that start with the base URI
        ContentDisposition cd = ContentDisposition.type("application/zip")
                .fileName(researchObjectId + "-" + Constants.versionId + ".zip").build();
        return ResearchObjectResource.addLinkHeaders(Response.ok(body), uriInfo, researchObjectId)
                .header("Content-disposition", cd).build();
    }
}
