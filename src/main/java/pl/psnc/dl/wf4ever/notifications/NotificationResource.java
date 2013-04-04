package pl.psnc.dl.wf4ever.notifications;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.db.dao.AtomFeedEntryDAO;

import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;

/**
 * Resource for Atom feed.
 * 
 * @author pejot
 * 
 */
@Path("notifications/")
public class NotificationResource {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(NotificationResource.class);


    /**
     * Get AtomFeed with list of entries.
     * 
     * @param uriInfo
     *            injected context infomration
     * @param roUri
     *            Research Object uri
     * @param from
     *            time - from
     * @param to
     *            time - to
     * @param limit
     *            max result number
     * @param source
     *            Atom Feed Entry source (rodl, checklist service, others maybe?)
     * @return Atom Feed with the list of requested entrires.
     */
    @GET
    @Produces(MediaType.APPLICATION_ATOM_XML)
    public Response getAtomFeed(@Context UriInfo uriInfo, @QueryParam("ro") URI roUri, @QueryParam("from") String from,
            @QueryParam("to") String to, @QueryParam("limit") Integer limit, @QueryParam("source") URI source) {
        AtomFeedEntryDAO entryDAO = new AtomFeedEntryDAO();
        //title depends on fitlers
        Feed feed = AtomFeed.createNewFeed("Some title", uriInfo.toString());
        feed.setEntries(entryDAO.findAll());
        WireFeedOutput wire = new WireFeedOutput();
        try {
            return Response.ok(wire.outputString(feed)).build();
        } catch (IllegalArgumentException | FeedException e) {
            LOGGER.error("Can not parse entries", e);
            return Response.serverError().build();
        }
    }
}
