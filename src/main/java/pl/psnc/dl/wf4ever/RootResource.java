package pl.psnc.dl.wf4ever;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.evo.CopyResource;
import pl.psnc.dl.wf4ever.evo.EvoInfoResource;
import pl.psnc.dl.wf4ever.evo.FinalizeResource;
import pl.psnc.dl.wf4ever.notifications.NotificationResource;
import pl.psnc.dl.wf4ever.vocabulary.NotificationService;
import pl.psnc.dl.wf4ever.vocabulary.ROEVOService;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.sun.jersey.api.view.Viewable;

/**
 * The base URI of RODL.
 * 
 * @author piotrekhol
 * 
 */
@Path("/")
public class RootResource {

    /**
     * Return the main HTML page.
     * 
     * @param uriInfo
     *            URI info
     * @return an HTML page
     */
    @GET
    @Produces("text/html")
    public Response index(@Context UriInfo uriInfo) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("version", ApplicationProperties.getVersion());
        map.put("rosrsuri", uriInfo.getAbsolutePathBuilder().path("ROs/").build());
        map.put("roevouri", uriInfo.getAbsolutePathBuilder().path("evo/").build());
        return Response.ok(new Viewable("/index", map)).build();
    }


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
    @Produces({ "application/rdf+xml", "text/turtle", "*/*" })
    public Response getServiceDescription(@Context UriInfo uriInfo, @HeaderParam("Accept") String accept) {
        RDFFormat format = RDFFormat.forMIMEType(accept, RDFFormat.RDFXML);

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        Resource service = model.createResource(uriInfo.getAbsolutePath().toString());
        URI baseNotificationUri = uriInfo.getAbsolutePathBuilder().path(NotificationResource.class).build();
        Literal notificationsTpl = model.createLiteral(baseNotificationUri.toString() + "{?ro}");
        service.addProperty(NotificationService.notifications, notificationsTpl);

        URI copyUri = uriInfo.getAbsolutePathBuilder().path(CopyResource.class).build();
        Literal copyTpl = model.createLiteral(copyUri.toString());
        URI finalizeUri = uriInfo.getAbsolutePathBuilder().path(FinalizeResource.class).build();
        Literal finalizeTpl = model.createLiteral(finalizeUri.toString());
        URI infoUri = uriInfo.getAbsolutePathBuilder().path(EvoInfoResource.class).build();
        Literal infoTpl = model.createLiteral(infoUri.toString() + "{?ro}");
        service.addProperty(ROEVOService.copy, copyTpl);
        service.addProperty(ROEVOService.finalize, finalizeTpl);
        service.addProperty(ROEVOService.info, infoTpl);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.write(out, format.getName().toUpperCase(), null);
        InputStream description = new ByteArrayInputStream(out.toByteArray());

        return Response.ok(description, format.getDefaultMIMEType()).build();
    }
}
