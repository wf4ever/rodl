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
public class ArchiveResearchObject extends FrozenResearchObject {

    /** logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(ArchiveResearchObject.class);


    /**
     * Constructor.
     * 
     * @param uri
     *            RO URI
     */
    public ArchiveResearchObject(URI uri, ResearchObject liveRO) {
        super(uri, liveRO);
    }


    /**
     * Get an Archive of existing Research Object.
     * 
     * @param uri
     *            uri
     * @param liveRO
     *            live Research Object
     * @return an existing Research Object or null
     * 
     */
    public static ArchiveResearchObject get(URI uri, ResearchObject LiveRO) {
        if (ROSRService.SMS.get().containsNamedGraph(uri.resolve(ResearchObject.MANIFEST_PATH))) {
            return new ArchiveResearchObject(uri, LiveRO);
        } else {
            return null;
        }
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
    public static ArchiveResearchObject create(URI uri, ResearchObject liveRO)
            throws ConflictException, DigitalLibraryException, AccessDeniedException {
        ArchiveResearchObject researchObject = new ArchiveResearchObject(uri, liveRO);
        InputStream manifest;
        try {
            ROSRService.SMS.get().createArchivedResearchObject(researchObject, liveRO);
            manifest = ROSRService.SMS.get().getManifest(researchObject, RDFFormat.RDFXML);
        } catch (IllegalArgumentException e) {
            // RO already existed in sms, maybe created by someone else
            throw new ConflictException("The RO with URI " + researchObject.getUri() + " already exists");
        }

        ROSRService.DL.get().createResearchObject(researchObject.getUri(), manifest,
            ArchiveResearchObject.MANIFEST_PATH, RDFFormat.RDFXML.getDefaultMIMEType());
        return researchObject;
    }


    @Override
    public void generateEvoInfo()
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        super.generateEvoInfo(EvoType.SNAPSHOT);
    }
}
