package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.vocabulary.ORE;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.ReadWrite;

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
     * @param user
     *            user creating the instance
     * @param uri
     *            manifest uri
     * @param researchObject
     *            research object being described
     */
    public Manifest(UserMetadata user, URI uri, ResearchObject researchObject) {
        super(user, uri);
        this.researchObject = researchObject;
        setNamedGraph(true);
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


    @Override
    public void save()
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        super.save();
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            Individual ro = model.getIndividual(researchObject.getUri().toString());
            if (ro != null) {
                abortTransaction(transactionStarted);
                throw new ConflictException("Research Object already exists: " + uri);
            }
            ro = model.createIndividual(researchObject.getUri().toString(), RO.ResearchObject);
            Individual manifest = model.createIndividual(uri.toString(), RO.Manifest);
            model.add(ro, ORE.isDescribedBy, manifest);
            model.add(manifest, ORE.describes, ro);

            saveAuthor(researchObject);
            saveAuthor(this);
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }


    public static Manifest create(UserMetadata user, URI uri, ResearchObject researchObject) {
        Manifest manifest = new Manifest(user, uri, researchObject);
        manifest.setCreator(user.getUri());
        manifest.setCreated(DateTime.now());
        return manifest;
    }

}
