/**
 * 
 */
package pl.psnc.dl.wf4ever.model.ROEVO;

import java.net.URI;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.common.EvoType;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
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

    protected ResearchObject liveRO;


    /**
     * Constructor.
     * 
     * @param uri
     *            RO URI
     */
    public FrozenResearchObject(UserMetadata user, Dataset dataset, boolean useTransactions, URI uri,
            ResearchObject liveRO) {
        super(user, dataset, useTransactions, uri);
        this.liveRO = liveRO;
    }


    protected void generateEvoInfo(EvoType type)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        ROSRService.SMS.get().generateEvoInformation(this, liveRO, type);
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
