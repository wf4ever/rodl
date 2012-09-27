package pl.psnc.dl.wf4ever.evo;

import java.io.InputStream;
import java.net.URI;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import pl.psnc.dl.wf4ever.rosrs.ROSRService;

@Path("evo/info")
public class EvoInfoResource {

    @Context
    HttpServletRequest request;

    @Context
    UriInfo uriInfo;


    @GET
    @Produces("text/turtle")
    public Response evoInfoContent(@QueryParam("ro") URI ResearchObjectURI) {
        //@TODO How to write the inforamtion in TTL format?
        InputStream stream = ROSRService.SMS.get().getEvoInfo(ResearchObjectURI);
        return Response.ok(stream).header("Content-Type", "text/turtle").build();
    }
}
