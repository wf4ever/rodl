package pl.psnc.dl.wf4ever.rosrs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.common.util.MemoryZipFile;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.exceptions.IncorrectModelException;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * A list of research objects REST APIs.
 * 
 * @author piotrhol
 * 
 */
@Path("ROs/")
public class ResearchObjectListResource {

    /** logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(ResearchObjectListResource.class);

    /** HTTP request. */
    @Context
    HttpServletRequest request;

    /** URI info. */
    @Context
    UriInfo uriInfo;


    /**
     * Returns list of relative links to research objects.
     * 
     * @return 200 OK
     */
    @GET
    @Produces("text/plain")
    public Response getResearchObjectList() {
        UserMetadata user = (UserMetadata) request.getAttribute(Constants.USER);

        Set<URI> list;
        if (user.getRole() == Role.PUBLIC) {
            list = ROSRService.SMS.get().findResearchObjects();
        } else {
            list = ROSRService.SMS.get().findResearchObjectsByCreator(user.getUri());
        }
        StringBuilder sb = new StringBuilder();
        for (URI id : list) {
            sb.append(id.toString());
            sb.append("\r\n");
        }

        ContentDisposition cd = ContentDisposition.type("text/plain").fileName("ROs.txt").build();

        return Response.ok().entity(sb.toString()).header("Content-disposition", cd).build();
    }


    /**
     * Creates new RO with given RO_ID.
     * 
     * @param researchObjectId
     *            slug header
     * @param accept
     *            Accept header
     * @return 201 (Created) when the RO was successfully created, 409 (Conflict) if the RO_ID is already used in the
     *         WORKSPACE_ID workspace
     * @throws BadRequestException
     *             the RO id is incorrect
     * @throws NotFoundException
     *             problem with creating the RO in dLibra
     * @throws DigitalLibraryException
     *             problem with creating the RO in dLibra
     * @throws ConflictException
     *             problem with creating the RO in dLibra
     * @throws UriBuilderException
     *             problem with creating the RO in dLibra
     * @throws IllegalArgumentException
     *             problem with creating the RO in dLibra
     * @throws AccessDeniedException
     *             no permissions
     */
    @POST
    public Response createResearchObject(@HeaderParam("Slug") String researchObjectId,
            @HeaderParam("Accept") String accept)
            throws BadRequestException, IllegalArgumentException, UriBuilderException, ConflictException,
            DigitalLibraryException, NotFoundException, AccessDeniedException {
        if (researchObjectId == null || researchObjectId.isEmpty()) {
            throw new BadRequestException("Research object ID is null or empty");
        }
        if (researchObjectId.contains("/")) {
            throw new BadRequestException("Research object ID cannot contain slashes, see WFE-703");
        }
        URI uri = uriInfo.getAbsolutePathBuilder().path(researchObjectId).path("/").build();
        ResearchObject researchObject = ResearchObject.create(uri);

        RDFFormat format = RDFFormat.forMIMEType(accept, RDFFormat.RDFXML);
        InputStream manifest = ROSRService.SMS.get().getNamedGraph(researchObject.getManifestUri(), format);
        ContentDisposition cd = ContentDisposition.type(format.getDefaultMIMEType())
                .fileName(ResearchObject.MANIFEST_PATH).build();

        return Response.created(researchObject.getUri()).entity(manifest).header("Content-disposition", cd).build();
    }


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
     * @throws IOException
     *             error when unzipping
     * @throws AccessDeniedException
     *             no permissions
     * @throws ConflictException
     *             RO already exists
     * @throws DigitalLibraryException
     *             error saving data to storage
     * @throws NotFoundException
     * @throws IncorrectModelException
     */
    @POST
    @Consumes("application/zip")
    public Response createResearchObjectFromZip(@HeaderParam("Slug") String researchObjectId, InputStream zipStream)
            throws BadRequestException, IOException, AccessDeniedException, ConflictException, DigitalLibraryException,
            NotFoundException, IncorrectModelException {
        if (researchObjectId == null || researchObjectId.isEmpty()) {
            throw new BadRequestException("Research object ID is null or empty");
        }

        UUID uuid = UUID.randomUUID();
        File tmpFile = File.createTempFile("tmp_ro", uuid.toString());
        BufferedInputStream inputStream = new BufferedInputStream(zipStream);
        FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
        IOUtils.copy(inputStream, fileOutputStream);
        URI roUri = uriInfo.getAbsolutePathBuilder().path(researchObjectId).path("/").build();
        ResearchObject researchObject = ResearchObject.create(roUri, new MemoryZipFile(tmpFile, researchObjectId));
        tmpFile.delete();
        return Response.created(researchObject.getUri()).build();
    }
}
