package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.ORE.ResourceMap;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.vocabulary.ORE;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.ReadWrite;

/**
 * ro:Manifest.
 * 
 */
public class Manifest extends ResourceMap {

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
    public Manifest(UserMetadata user, URI uri, ResearchObject researchObject, URI creator, DateTime created) {
        super(user, researchObject, uri, creator, created);
        setNamedGraph(true);
    }


    @Override
    public void save()
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        super.save();
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            Individual ro = model.getIndividual(aggregation.getUri().toString());
            if (ro != null) {
                abortTransaction(transactionStarted);
                throw new ConflictException("Research Object already exists: " + uri);
            }
            ro = model.createIndividual(aggregation.getUri().toString(), RO.ResearchObject);
            Individual manifest = model.createIndividual(uri.toString(), RO.Manifest);
            model.add(ro, ORE.isDescribedBy, manifest);
            model.add(manifest, ORE.describes, ro);

            saveAuthor((Thing) aggregation);
            saveAuthor(this);
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }


    public static Manifest create(UserMetadata user, URI uri, ResearchObject researchObject) {
        Manifest manifest = new Manifest(user, uri, researchObject, user.getUri(), DateTime.now());
        return manifest;
    }


    public void saveRoResourceClass(Resource resource) {
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            model.createIndividual(resource.getUri().toString(), RO.Resource);
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }

}
