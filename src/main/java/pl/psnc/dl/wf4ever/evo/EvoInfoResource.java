package pl.psnc.dl.wf4ever.evo;

import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import pl.psnc.dl.wf4ever.common.ResearchObject;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;
import pl.psnc.dl.wf4ever.rosrs.ResearchObjectFactory;

/**
 * REST API resource to get the evolution information of an RO.
 * 
 * @author piotrekhol
 * 
 */
@Path("evo/info")
public class EvoInfoResource {

    /**
     * Get the evolution information of an RO.
     * 
     * @param researchObjectURI
     *            RO URI
     * @return ROEVO info in Turtle format
     */
    @GET
    @Produces("text/turtle")
    public Response evoInfoContent(@QueryParam("ro") URI researchObjectURI) {
        ResearchObject researchObject = ResearchObjectFactory.get(researchObjectURI);
        //@TODO How to write the inforamtion in TTL format?
        InputStream stream = ROSRService.SMS.get().getEvoInfo(researchObject);
        return Response.ok(stream).header("Content-Type", "text/turtle").build();
    }
}
