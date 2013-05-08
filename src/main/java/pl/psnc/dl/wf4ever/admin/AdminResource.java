package pl.psnc.dl.wf4ever.admin;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.auth.RequestAttribute;
import pl.psnc.dl.wf4ever.db.ResearchObjectId;
import pl.psnc.dl.wf4ever.db.dao.ResearchObjectIdDAO;
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


    @POST
    @Path("solr/reindex/")
    public String solrReindex() {
        for (ResearchObject ro : ResearchObject.getAll(builder, null)) {
            ro.updateIndexAttributes();
        }
        return "Operation succeed";

    }


    @POST
    @Path("preservrvetion/id/synchronize")
    public String synchronizeIds() {
        List<URI> storedRosList = new ArrayList<URI>();
        for (ResearchObject ro : ResearchObject.getAll(builder, null)) {
            storedRosList.add(ro.getUri());
        }
        //@TODO
        //for list od dArce 
        //check if uri is in the list
        // if it isn't, add.

        ResearchObjectIdDAO dao = new ResearchObjectIdDAO();
        for (ResearchObjectId id : dao.all()) {
            if (!storedRosList.contains(id.getId())) {
                dao.delete(id);
            }
        }
        for (URI rui : storedRosList) {
            if (dao.findByPrimaryKey(rui) == null) {
                dao.save(new ResearchObjectId(rui));
            }
        }
        return "Operation succeed";
    }
}
