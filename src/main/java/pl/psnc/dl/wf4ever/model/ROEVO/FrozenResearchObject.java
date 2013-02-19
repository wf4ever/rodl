/**
 * 
 */
package pl.psnc.dl.wf4ever.model.ROEVO;

import java.net.URI;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.common.db.EvoType;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;

import com.hp.hpl.jena.query.Dataset;

/**
 * A DAO for a research object.
 * 
 * @author piotrekhol
 * 
 */
public class FrozenResearchObject extends ResearchObject {

    /** logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(FrozenResearchObject.class);

    /** the research object that is snapshotted. */
    protected ResearchObject liveRO;


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     * @param dataset
     *            custom dataset
     * @param useTransactions
     *            should transactions be used. Note that not using transactions on a dataset which already uses
     *            transactions may make it unreadable.
     * @param uri
     *            RO URI
     * @param liveRO
     *            the research object that is snapshotted
     */
    public FrozenResearchObject(UserMetadata user, Dataset dataset, boolean useTransactions, URI uri,
            ResearchObject liveRO) {
        super(user, dataset, useTransactions, uri);
        this.liveRO = liveRO;
    }


    /**
     * Generate evolution information.
     * 
     * @param type
     *            snapshot or archive
     */
    protected void generateEvoInfo(EvoType type) {
        ROSRService.SMS.get().generateEvoInformation(this, liveRO, type);
        //HACK
        this.getAggregatedResources().put(getFixedEvolutionAnnotationBodyUri(),
            (AggregatedResource) getEvoInfoAnnotation().getBody());
        this.getEvoInfoBody().serialize();
        this.getManifest().serialize();
        if (liveRO != null) {
            liveRO.getEvoInfoBody().serialize();
            liveRO.getManifest().serialize();
        }
    }


    public ResearchObject getLiveRO() {
        return liveRO;
    }
}
