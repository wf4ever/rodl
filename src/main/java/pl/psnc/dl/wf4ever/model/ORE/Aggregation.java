package pl.psnc.dl.wf4ever.model.ORE;

import java.net.URI;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;

/**
 * ore:Aggregation, for example ro:ResearchObject and ro:Folder.
 * 
 * @author piotrekhol
 * 
 */
public interface Aggregation {

    URI getUri();


    /**
     * Get resource map describing this aggregation.
     * 
     * @return
     * @throws NotFoundException
     * @throws AccessDeniedException
     * @throws DigitalLibraryException
     * @throws ConflictException
     */
    ResourceMap getResourceMap()
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException;

}
