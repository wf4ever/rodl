package pl.psnc.dl.wf4ever.admin;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.auth.RequestAttribute;
import pl.psnc.dl.wf4ever.evo.CopyOperation;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

/**
 * The admin namespace for protected functions provided via API.
 * 
 * @author pejot
 * 
 */
@Path("admin/")
public class AdminResource {

    /** Resource builder. */
    @RequestAttribute("Builder")
    private Builder builder;

    /** logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(CopyOperation.class);


    /**
     * Reindex solr data.
     * 
     * @return comunicate.
     */
    @POST
    @Path("solr/reindex/")
    public String solrReindex() {
        for (ResearchObject ro : ResearchObject.getAll(builder, null)) {
            ro.updateIndexAttributes();
        }
        return "Operation succeed";

    }
}
