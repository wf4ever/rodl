package pl.psnc.dl.wf4ever.evo;

import java.util.UUID;

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
@Path("evo/")
public class EvoResource {

    @SuppressWarnings("unused")
    private final static Logger logger = Logger.getLogger(EvoResource.class);

    @Context
    HttpServletRequest request;

    @Context
    UriInfo uriInfo;


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
    public Response createCopy(RoCopy copy)
            throws BadRequestException {
        if (copy.getCopyFrom() == null) {
            throw new BadRequestException("incorrect or missing \"copyfrom\" attribute");
        }
        if (copy.getType() == null) {
            throw new BadRequestException("incorrect or missing \"type\" attribute");
        }
        String id = request.getHeader(Constants.SLUG_HEADER);
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        return EvoService.copyRO(copy, id);
    }
}
