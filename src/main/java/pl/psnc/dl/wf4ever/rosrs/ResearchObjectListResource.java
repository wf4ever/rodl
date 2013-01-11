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
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.auth.RequestAttribute;
import pl.psnc.dl.wf4ever.common.Builder;
import pl.psnc.dl.wf4ever.common.util.MemoryZipFile;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
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

    /** Authenticated user. */
    @RequestAttribute("User")
    private UserMetadata user;

    /** Resource builder. */
    @RequestAttribute("Builder")
    private Builder builder;


    /**
     * Returns list of relative links to research objects.
     * 
     * @return 200 OK
     */
    @GET
    @Produces("text/plain")
    public Response getResearchObjectList() {
        Set<ResearchObject> list = ResearchObject.getAll(builder);
        StringBuilder sb = new StringBuilder();
        for (ResearchObject id : list) {
            sb.append(id.getUri().toString());
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
     */
    @POST
    public Response createResearchObject(@HeaderParam("Slug") String researchObjectId,
            @HeaderParam("Accept") String accept)
            throws BadRequestException {
        if (researchObjectId == null || researchObjectId.isEmpty()) {
            throw new BadRequestException("Research object ID is null or empty");
        }
        if (researchObjectId.contains("/")) {
            throw new BadRequestException("Research object ID cannot contain slashes, see WFE-703");
        }
        URI uri = uriInfo.getAbsolutePathBuilder().path(researchObjectId).path("/").build();
        ResearchObject researchObject = ResearchObject.create(builder, uri);

        RDFFormat format = accept != null ? RDFFormat.forMIMEType(accept, RDFFormat.RDFXML) : RDFFormat.RDFXML;
        InputStream manifest = researchObject.getManifest().getGraphAsInputStream(format);
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
     */
    @POST
    @Consumes("application/zip")
    public Response createResearchObjectFromZip(@HeaderParam("Slug") String researchObjectId, InputStream zipStream)
            throws BadRequestException, IOException {
        if (researchObjectId == null || researchObjectId.isEmpty()) {
            throw new BadRequestException("Research object ID is null or empty");
        }

        UUID uuid = UUID.randomUUID();
        File tmpFile = File.createTempFile("tmp_ro", uuid.toString());
        BufferedInputStream inputStream = new BufferedInputStream(zipStream);
        FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
        IOUtils.copy(inputStream, fileOutputStream);
        URI roUri = uriInfo.getAbsolutePathBuilder().path(researchObjectId).path("/").build();
        ResearchObject researchObject = ResearchObject.create(builder, roUri, new MemoryZipFile(tmpFile,
                researchObjectId));
        tmpFile.delete();
        return Response.created(researchObject.getUri()).build();
    }
}
