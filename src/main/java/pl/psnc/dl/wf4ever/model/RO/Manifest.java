package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.model.RDF.Thing;

/**
 * ro:Manifest.
 * 
 */
public class Manifest extends Thing {

    /**
     * RO that this manifest describes.
     */
    private ResearchObject researchObject;


    /**
     * Constructor.
     * 
     * @param uri
     *            manifest uri
     * @param researchObject
     *            research object being described
     */
    public Manifest(URI uri, ResearchObject researchObject) {
        super(uri);
        this.researchObject = researchObject;
    }


    /**
     * Store to disk.
     * 
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public void serialize()
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        serialize(researchObject);
    }
}
