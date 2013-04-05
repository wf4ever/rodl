package pl.psnc.dl.wf4ever.notifications;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.db.AtomFeedEntry;
import pl.psnc.dl.wf4ever.db.dao.AtomFeedEntryDAO;
import pl.psnc.dl.wf4ever.vocabulary.NotificationService;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
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
     * Get a service description as an RDF graph.
     * 
     * @param uriInfo
     *            injected context information
     * @param accept
     *            accept header
     * @return RDF service description, format subject to content-negotiation
     */
    @GET
    public Response getServiceDescription(@Context UriInfo uriInfo, @HeaderParam("Accept") String accept) {
        RDFFormat format = RDFFormat.forMIMEType(accept, RDFFormat.RDFXML);

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        Resource service = model.createResource(uriInfo.getAbsolutePath().toString());
        URI baseNotificationUri = uriInfo.getAbsolutePathBuilder().path(getClass(), "getAtomFeeds").build();
        Literal notificationsTpl = model.createLiteral(baseNotificationUri.toString() + "{?ro}");
        service.addProperty(NotificationService.notifications, notificationsTpl);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.write(out, format.getName().toUpperCase(), null);
        InputStream description = new ByteArrayInputStream(out.toByteArray());

        return Response.ok(description, format.getDefaultMIMEType()).build();
    }


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
     *            time - to
     * @return Atom Feed with the list of requested entrires.
     */
    @GET
    @Path("notifications/")
    @Produces(MediaType.APPLICATION_ATOM_XML)
    public Response getAtomFeeds(@Context UriInfo uriInfo, @QueryParam("ro") URI roUri,
            @QueryParam("from") String from, @QueryParam("to") String to) {
        AtomFeedEntryDAO entryDAO = new AtomFeedEntryDAO();
        //title depends on filters
        Date dateFrom = (from != null) ? DateTime.parse(from).toDate() : null;
        Date dateTo = (to != null) ? DateTime.parse(to).toDate() : null;
        Feed feed = AtomFeed.createNewFeed(AtomFeedTitileBuilder.buildTitle(roUri, dateFrom, dateTo), uriInfo
                .getRequestUri().toString());
        feed.setEntries(AtomFeedEntryDAO.convertToRawEntry(entryDAO.find(roUri, dateFrom, dateTo)));
        WireFeedOutput wire = new WireFeedOutput();
        try {
            return Response.ok(wire.outputString(feed)).build();
        } catch (IllegalArgumentException | FeedException e) {
            LOGGER.error("Can not parse entries", e);
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
     * @return Atom Feed with the list of requested entrires.
     */
    @DELETE
    @Path("notifications/")
    public Response deleteAtomFeeds(@QueryParam("ro") URI roUri, @QueryParam("from") Date from,
            @QueryParam("to") Date to) {
        AtomFeedEntryDAO entryDAO = new AtomFeedEntryDAO();
        for (AtomFeedEntry entry : entryDAO.find(roUri, from, to)) {
            entryDAO.delete(entry);
        }
        return Response.ok().build();
    }
}
