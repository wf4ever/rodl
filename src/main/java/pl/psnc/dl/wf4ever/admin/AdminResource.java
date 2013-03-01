package pl.psnc.dl.wf4ever.admin;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * The admin namespace for protected functions provided via API.
 * 
 * @author pejot
 * 
 */
@Path("admin/")
public class AdminResource {

    @POST
    @Path("solr/reindex/")
    public void solrReindex() {
        //getListOfAllResearchObject
        //forceSaveThem??
        //for (ResearchObject ro : ResearchObject.getAll(, null)) {

        //}
    }
}
