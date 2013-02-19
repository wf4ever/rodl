/**
 * 
 */
package pl.psnc.dl.wf4ever.model.ROEVO;

import java.net.URI;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.common.db.EvoType;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
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
public class SnapshotResearchObject extends FrozenResearchObject {

    /** logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(SnapshotResearchObject.class);


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
    public SnapshotResearchObject(UserMetadata user, Dataset dataset, boolean useTransactions, URI uri,
            ResearchObject liveRO) {
        super(user, dataset, useTransactions, uri, liveRO);
    }


    /**
     * Get a Snapshot of existing Research Object.
     * 
     * @param builder
     *            user creating the instance
     * @param uri
     *            uri
     * @param liveRO
     *            live Research Object
     * @return an existing Research Object or null
     */
    public static SnapshotResearchObject get(Builder builder, URI uri, ResearchObject liveRO) {
        SnapshotResearchObject researchObject = builder.buildSnapshotResearchObject(uri, liveRO);
        if (researchObject.getManifest().isNamedGraph()) {
            return researchObject;
        } else {
            return null;
        }
    }


    @Override
    public void generateEvoInfo()
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        super.generateEvoInfo(EvoType.SNAPSHOT);
    }

}
