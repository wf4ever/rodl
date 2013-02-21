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
 * The builder setting properties of an archived resource.
 * 
 * @author piotrekhol
 * 
 */
public class ArchiveBuilder implements EvoBuilder {

    @Override
    public void setFrozenAt(Thing resource, DateTime time) {
        resource.setArchivedAt(time);
    }


    @Override
    public void setFrozenBy(Thing resource, UserMetadata user) {
        resource.setArchivedBy(user);
    }


    @Override
    public void setIsCopyOf(Thing resource, Thing original) {
        resource.setArchiveOf(original);
    }


    @Override
    public void saveRDFType(OntModel model, ImmutableResearchObject researchObject) {
        Individual ro = model.getIndividual(researchObject.getUri().toString());
        ro.addRDFType(ROEVO.ArchivedRO);
    }


    @Override
    public void saveHasLive(OntModel model, ImmutableResearchObject researchObject) {
        Individual ro = model.getIndividual(researchObject.getUri().toString());
        Individual live = model.createIndividual(researchObject.getLiveRO().getUri().toString(), ROEVO.LiveRO);
        ro.addProperty(ROEVO.isArchiveOf, live);
    }


    @Override
    public void saveFrozenAt(OntModel model, ImmutableResearchObject researchObject) {
        Individual ro = model.getIndividual(researchObject.getUri().toString());
        Literal date = model.createLiteral(researchObject.getArchivedAt().toString());
        ro.addProperty(ROEVO.archivedAtTime, date);
    }


    @Override
    public void saveFrozenBy(OntModel model, ImmutableResearchObject researchObject) {
        Individual ro = model.getIndividual(researchObject.getUri().toString());
        if (!ro.hasProperty(ROEVO.archivedBy) && researchObject.getArchivedBy() != null) {
            Individual author = model.createIndividual(researchObject.getArchivedBy().getUri().toString(), FOAF.Agent);
            ro.addProperty(ROEVO.archivedBy, author);
            author.setPropertyValue(FOAF.name, model.createLiteral(researchObject.getCreator().getName()));
        }
    }


    @Override
    public void saveHasFrozen(OntModel model, ImmutableResearchObject researchObject) {
        Individual ro = model.createIndividual(researchObject.getUri().toString(), ROEVO.ArchivedRO);
        Individual live = model.createIndividual(researchObject.getLiveRO().getUri().toString(), ROEVO.LiveRO);
        ro.addProperty(ROEVO.hasArchive, live);
    }

}
