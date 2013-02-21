package pl.psnc.dl.wf4ever.model;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.ROEVO.ImmutableResearchObject;
import pl.psnc.dl.wf4ever.vocabulary.FOAF;
import pl.psnc.dl.wf4ever.vocabulary.ROEVO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;

/**
 * The builder setting properties of a snapshotted resource.
 * 
 * @author piotrekhol
 * 
 */
public class SnapshotBuilder implements EvoBuilder {

    @Override
    public void setFrozenAt(Thing resource, DateTime time) {
        resource.setSnapshottedAt(time);
    }


    @Override
    public void setFrozenBy(Thing resource, UserMetadata user) {
        resource.setSnapshottedBy(user);
    }


    @Override
    public void setIsCopyOf(Thing resource, Thing original) {
        resource.setSnapshotOf(original);
    }


    @Override
    public void saveRDFType(OntModel model, ImmutableResearchObject researchObject) {
        Individual ro = model.getIndividual(researchObject.getUri().toString());
        ro.addRDFType(ROEVO.SnapshotRO);
    }


    @Override
    public void saveHasLive(OntModel model, ImmutableResearchObject researchObject) {
        Individual ro = model.getIndividual(researchObject.getUri().toString());
        Individual live = model.createIndividual(researchObject.getLiveRO().getUri().toString(), ROEVO.LiveRO);
        ro.addProperty(ROEVO.isSnapshotOf, live);
    }


    @Override
    public void saveFrozenAt(OntModel model, ImmutableResearchObject researchObject) {
        Individual ro = model.getIndividual(researchObject.getUri().toString());
        Literal date = model.createLiteral(researchObject.getSnapshottedAt().toString());
        ro.addProperty(ROEVO.snapshotedAtTime, date);
    }


    @Override
    public void saveFrozenBy(OntModel model, ImmutableResearchObject researchObject) {
        Individual ro = model.getIndividual(researchObject.getUri().toString());
        if (!ro.hasProperty(ROEVO.snapshotedBy) && researchObject.getSnapshottedBy() != null) {
            Individual author = model.createIndividual(researchObject.getSnapshottedBy().getUri().toString(),
                FOAF.Agent);
            ro.addProperty(ROEVO.snapshotedBy, author);
            author.setPropertyValue(FOAF.name, model.createLiteral(researchObject.getCreator().getName()));
        }
    }


    @Override
    public void saveHasFrozen(OntModel model, ImmutableResearchObject researchObject) {
        Individual ro = model.createIndividual(researchObject.getUri().toString(), ROEVO.SnapshotRO);
        Individual live = model.createIndividual(researchObject.getLiveRO().getUri().toString(), ROEVO.LiveRO);
        ro.addProperty(ROEVO.hasSnapshot, live);
    }

}
