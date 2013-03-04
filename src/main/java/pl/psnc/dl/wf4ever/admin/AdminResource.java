package pl.psnc.dl.wf4ever.admin;

import java.net.URI;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import pl.psnc.dl.wf4ever.auth.RequestAttribute;
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


    @POST
    @Path("solr/reindex/")
    public void solrReindex() {
        for (ResearchObject ro : ResearchObject.getAll(builder, null)) {
            Multimap<URI, Object> roDescription = ro.getManifest().getDescriptionFor(ro.getUri());
            for (Annotation annotation : ro.getAnnotations().values()) {
                roDescription.putAll(annotation.getBody().getDescriptionFor(ro.getUri()));
            }
            SearchServer searchServer = SolrSearchServer.get();
            searchServer.saveROAttributes(ro.getUri(), roDescription);
        }

    }
}