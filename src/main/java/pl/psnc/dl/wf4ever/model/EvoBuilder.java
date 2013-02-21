package pl.psnc.dl.wf4ever.model;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.ROEVO.ImmutableResearchObject;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * Builder saving snapshot or archival metadata in resources.
 * 
 * Snapshot or archival operations are called "freeze" for simplicity.
 * 
 * @author piotrekhol
 * 
 */
public interface EvoBuilder {

    /**
     * Set the snapshot or archival time.
     * 
     * @param resource
     *            the resource that is snapshotted/archived
     * @param time
     *            the time of snapshot or archival
     */
    void setFrozenAt(Thing resource, DateTime time);


    /**
     * Set the user making the snapshot or archive.
     * 
     * @param resource
     *            the resource that is snapshotted/archived
     * @param user
     *            the user performing the snapshotting or archival
     */
    void setFrozenBy(Thing resource, UserMetadata user);


    /**
     * Set that one resource is a snapshot or archive of another.
     * 
     * @param resource
     *            the resource that is snapshotted/archived
     * @param original
     *            the original resource
     */
    void setIsCopyOf(Thing resource, Thing original);


    void saveRDFType(OntModel model, ImmutableResearchObject researchObject);


    void saveHasLive(OntModel model, ImmutableResearchObject researchObject);


    void saveFrozenAt(OntModel model, ImmutableResearchObject researchObject);


    void saveFrozenBy(OntModel model, ImmutableResearchObject researchObject);


    void saveHasFrozen(OntModel model, ImmutableResearchObject researchObject);
}
