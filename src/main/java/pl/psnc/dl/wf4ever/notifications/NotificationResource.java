package pl.psnc.dl.wf4ever.notifications;

import java.net.URI;
import java.util.Date;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.db.dao.AtomFeedEntryDAO;
import pl.psnc.dl.wf4ever.notifications.NotificationFeed.FeedType;
import pl.psnc.dl.wf4ever.notifications.NotificationFeed.Title;

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
     *            injected context information
     * @param roUri
     *            Research Object URI
     * @param from
     *            time - from
     * @param to
     *            time - to <<<<<<< Upstream, based on my-develop
     * @param source
     *            feed field source
     * @param limit
     *            max number of returned entry
     * @return Atom Feed with the list of requested entries.
     */
    @GET
    @Produces(MediaType.APPLICATION_ATOM_XML)
    public Response getAtomFeeds(@Context UriInfo uriInfo, @QueryParam("ro") URI roUri,
            @QueryParam("from") String from, @QueryParam("to") String to, @QueryParam("source") URI source,
            @QueryParam("limit") Integer limit) {
        AtomFeedEntryDAO entryDAO = new AtomFeedEntryDAO();
        //title depends on filters
        Date dateFrom = (from != null) ? DateTime.parse(from).toDate() : null;
        Date dateTo = (to != null) ? DateTime.parse(to).toDate() : null;
        String id = uriInfo.getRequestUri().toString();
        String title = Title.build(roUri, dateFrom, dateTo);
        List<Notification> entries = entryDAO.find(roUri, dateFrom, dateTo, source, limit);
        NotificationFeed atomFeed = new NotificationFeed.Builder(id).title(title).entries(entries).build();
        Feed feed = atomFeed.asFeed(FeedType.ATOM_1_0, uriInfo.getRequestUri());
        WireFeedOutput wire = new WireFeedOutput();
        try {
            return Response.ok(wire.outputString(feed)).build();
        } catch (IllegalArgumentException | FeedException e) {
            LOGGER.error("Cannot parse entries", e);
            return Response.serverError().build();
        }
    }


    /**
     * Delete Atom feed entries.
     * 
     * @param roUri
     *            Research Object uri
     * @param from
     *            time - from
     * @param to
     *            time - to
     * @param source
     *            feed field source
     * @param limit
     *            max number of returned entry
     * @return Atom Feed with the list of requested entrires.
     */
    @DELETE
    public Response deleteAtomFeeds(@QueryParam("ro") URI roUri, @QueryParam("from") Date from,
            @QueryParam("to") Date to, @QueryParam("source") URI source, @QueryParam("limit") Integer limit) {
        AtomFeedEntryDAO entryDAO = new AtomFeedEntryDAO();
        for (Notification entry : entryDAO.find(roUri, from, to, source, limit)) {
            entryDAO.delete(entry);
        }
        return Response.ok().build();
    }
}
