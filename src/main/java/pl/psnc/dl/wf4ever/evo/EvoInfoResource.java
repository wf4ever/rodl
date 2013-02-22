package pl.psnc.dl.wf4ever.evo;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import pl.psnc.dl.wf4ever.auth.RequestAttribute;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

/**
 * REST API resource to get the evolution information of an RO.
 * 
 * @author piotrekhol
 * 
 */
@Path("evo/info")
public class EvoInfoResource {

    /** Resource builder. */
    @RequestAttribute("Builder")
    private Builder builder;


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
        ResearchObject researchObject = ResearchObject.get(builder, researchObjectURI);
        if (researchObject == null) {
            new NotFoundException("Research Object not found");
        }
        return Response.ok(researchObject.getEvoInfo().getSerialization()).header("Content-Type", "text/turtle")
                .build();
    }
}
