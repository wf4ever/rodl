/**
 * 
 */
package pl.psnc.dl.wf4ever.model.ROEVO;

import java.net.URI;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.common.db.EvoType;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

import com.hp.hpl.jena.query.Dataset;

/**
 * A DAO for a research object.
 * 
 * @author piotrekhol
 * 
 */
public class ArchiveResearchObject extends FrozenResearchObject {

    /** logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(ArchiveResearchObject.class);


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
    public ArchiveResearchObject(UserMetadata user, Dataset dataset, boolean useTransactions, URI uri,
            ResearchObject liveRO) {
        super(user, dataset, useTransactions, uri, liveRO);
    }


    /**
     * Get an Archive of existing Research Object.
     * 
     * @param builder
     *            model instance builder
     * @param uri
     *            uri
     * @param liveRO
     *            live Research Object
     * @return an existing Research Object or null
     * 
     */
    public static ArchiveResearchObject get(Builder builder, URI uri, ResearchObject liveRO) {
        ArchiveResearchObject researchObject = builder.buildArchiveResearchObject(uri, liveRO);
        if (researchObject.getManifest().isNamedGraph()) {
            return researchObject;
        } else {
            return null;
        }
    }


    @Override
    public void generateEvoInfo() {
        super.generateEvoInfo(EvoType.ARCHIVE);
    }
}
