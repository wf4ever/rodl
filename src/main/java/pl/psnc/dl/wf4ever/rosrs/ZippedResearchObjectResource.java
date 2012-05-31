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
import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
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

    private static final String workspaceId = "default";

    private static final String versionId = "v1";


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
        UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
        DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(user.getLogin(), user.getPassword());

        InputStream body = dl.getZippedVersion(workspaceId, researchObjectId, versionId);
        //TODO add all named graphs from SMS that start with the base URI
        ContentDisposition cd = ContentDisposition.type("application/zip")
                .fileName(researchObjectId + "-" + versionId + ".zip").build();
        return Response.ok(body).header("Content-disposition", cd).build();
    }
}
