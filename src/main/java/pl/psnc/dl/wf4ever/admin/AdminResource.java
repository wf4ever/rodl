package pl.psnc.dl.wf4ever.admin;

import java.net.URI;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.auth.RequestAttribute;
import pl.psnc.dl.wf4ever.evo.CopyOperation;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.searchserver.SearchServer;
import pl.psnc.dl.wf4ever.searchserver.solr.SolrSearchServer;

import com.google.common.collect.Multimap;

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
            try {
                Multimap<URI, Object> roDescription = ro.getManifest().getDescriptionFor(ro.getUri());
                for (Annotation annotation : ro.getAnnotations().values()) {
                    roDescription.putAll(annotation.getBody().getDescriptionFor(ro.getUri()));
                }
                SearchServer searchServer = SolrSearchServer.get();
                searchServer.saveROAttributes(ro.getUri(), roDescription);
            } catch (Throwable e) {
                LOGGER.error("Can not read to index " + ro.getUri(), e);
            }
        }
        return "Operation succeed";

    }
}
