/**
 * 
 */
package pl.psnc.dl.wf4ever.model.ROEVO;

import java.io.InputStream;
import java.net.URI;

import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.common.EvoType;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;

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
    public FrozenResearchObject(URI uri, ResearchObject liveRO) {
        super(uri);
        this.liveRO = liveRO;
    }


    /**
     * Create new Research Object.
     * 
     * @param uri
     *            RO URI
     * @param liveRO
     *            live RO
     * @return an instance
     * @throws ConflictException
     *             the research object URI is already used
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             no permissions
     */
    public static FrozenResearchObject create(URI uri, ResearchObject liveRO)
            throws ConflictException, DigitalLibraryException, AccessDeniedException {
        FrozenResearchObject researchObject = new FrozenResearchObject(uri, liveRO);
        InputStream manifest;
        try {
            ROSRService.SMS.get().createArchivedResearchObject(researchObject, liveRO);
            manifest = ROSRService.SMS.get().getManifest(researchObject, RDFFormat.RDFXML);
        } catch (IllegalArgumentException e) {
            // RO already existed in sms, maybe created by someone else
            throw new ConflictException("The RO with URI " + researchObject.getUri() + " already exists");
        }

        ROSRService.DL.get().createResearchObject(researchObject.getUri(), manifest,
            FrozenResearchObject.MANIFEST_PATH, RDFFormat.RDFXML.getDefaultMIMEType());
        return researchObject;
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
