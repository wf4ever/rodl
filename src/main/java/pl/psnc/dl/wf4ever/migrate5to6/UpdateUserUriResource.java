package pl.psnc.dl.wf4ever.migrate5to6;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.BadRequestException;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;

/**
 * A helper, temporary class for updating the OpenID. Used for OpenID 2, i.e. Google OpenIDs.
 * 
 * @author piotrhol
 * 
 */
@Path("userUpdate/")
public class UpdateUserUriResource {

    /** logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(UpdateUserUriResource.class);


    /**
     * Call the user URI replace method.
     * 
     * @param content
     *            old URI in first line, new URI in second
     * @return 200 OK with a number of triples updated
     * @throws NamingException
     *             could not complete the update
     * @throws SQLException
     *             could not complete the update
     * @throws BadRequestException
     *             could not complete the update
     * @throws URISyntaxException
     *             could not complete the update
     */
    @POST
    @Produces("text/plain")
    public Response updateUserURI(String content)
            throws NamingException, SQLException, BadRequestException, URISyntaxException {
        String[] ids = content.split("\r\n");
        if (ids.length < 2) {
            throw new BadRequestException("Must provide 2 URIs in separate lines");
        }
        int cnt = ROSRService.SMS.get().changeURI(new URI(ids[0]), new URI(ids[1]));

        return Response.ok().entity("" + cnt + " quads updated.").build();
    }
}
