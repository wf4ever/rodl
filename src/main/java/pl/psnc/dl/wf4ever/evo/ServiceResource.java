package pl.psnc.dl.wf4ever.evo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.vocabulary.ROEVOService;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * REST API resource for the root of the service.
 * 
 * @author piotrhol
 * 
 */
@Path("evo/")
public class ServiceResource {

    /** logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(ServiceResource.class);

    /** URI info. */
    @Context
    private UriInfo uriInfo;


    /**
     * Get a service description as an RDF graph.
     * 
     * @param accept
     *            accept header
     * @return RDF service description, format subject to content-negotiation
     */
    @GET
    public Response getServiceDescription(@HeaderParam("Accept") String accept) {
        RDFFormat format = RDFFormat.forMIMEType(accept, RDFFormat.RDFXML);

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        Resource service = model.createResource(uriInfo.getAbsolutePath().toString());
        Resource copyR = model.createResource(uriInfo.getAbsolutePathBuilder().path("copy/").build().toString());
        Resource finalizeR = model
                .createResource(uriInfo.getAbsolutePathBuilder().path("finalize/").build().toString());
        Literal infoTpl = model.createLiteral(uriInfo.getAbsolutePathBuilder().path("info").build().toString()
                + "{?ro}");
        service.addProperty(ROEVOService.copy, copyR);
        service.addProperty(ROEVOService.finalize, finalizeR);
        service.addProperty(ROEVOService.info, infoTpl);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.write(out, format.getName().toUpperCase(), null);
        InputStream description = new ByteArrayInputStream(out.toByteArray());

        return Response.ok(description, format.getDefaultMIMEType()).build();
    }
}
