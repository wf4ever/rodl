package pl.psnc.dl.wf4ever.migrate5to6;

import java.sql.SQLException;

import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.rosrs.ROSRService;

/**
 * A helper, temporary class for migrating content from ROSRS 5 to RODL 6.
 * 
 * @author piotrhol
 * 
 */
@Path("5to6/")
public class Migrate5to6Resource {

    /** logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(Migrate5to6Resource.class);


    /**
     * Receive the request to do the migration.
     * 
     * @return 200 OK with a number of triples copied.
     * @throws NamingException
     *             could not copy
     * @throws SQLException
     *             could not copy
     */
    @GET
    @Produces("text/plain")
    public Response migrate5to6()
            throws NamingException, SQLException {
        int cnt = ROSRService.SMS.get().migrateRosr5To6("java:/comp/env/jdbc/rosrs");

        return Response.ok().entity("" + cnt + " new quads copied.").build();
    }

}
