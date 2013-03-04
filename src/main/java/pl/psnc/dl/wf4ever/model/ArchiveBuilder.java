package pl.psnc.dl.wf4ever.model;

import java.net.URI;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.model.ROEVO.ImmutableResearchObject;
import pl.psnc.dl.wf4ever.vocabulary.FOAF;
import pl.psnc.dl.wf4ever.vocabulary.ROEVO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * The builder setting properties of an archived resource.
 * 
 * @author piotrekhol
 * 
 */
public class ArchiveBuilder extends EvoBuilder {

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
    public void saveCopyDateTime(OntModel model, ImmutableResearchObject researchObject) {
        Individual ro = model.getIndividual(researchObject.getUri().toString());
        Literal date = model.createLiteral(researchObject.getCopyDateTime().toString());
        ro.addProperty(ROEVO.archivedAtTime, date);
    }


    @Override
    public void saveCopyAuthor(OntModel model, ImmutableResearchObject researchObject) {
        Individual ro = model.getIndividual(researchObject.getUri().toString());
        if (!ro.hasProperty(ROEVO.archivedBy) && researchObject.getCopyAuthor() != null) {
            Individual author = model.createIndividual(researchObject.getCopyAuthor().getUri().toString(), FOAF.Agent);
            ro.addProperty(ROEVO.archivedBy, author);
            author.setPropertyValue(FOAF.name, model.createLiteral(researchObject.getCreator().getName()));
        }
    }


    @Override
    public void saveHasCopy(OntModel model, ImmutableResearchObject researchObject) {
        Individual ro = model.createIndividual(researchObject.getUri().toString(), ROEVO.ArchivedRO);
        Individual live = model.createIndividual(researchObject.getLiveRO().getUri().toString(), ROEVO.LiveRO);
        ro.addProperty(ROEVO.isArchiveOf, live);
        live.addProperty(ROEVO.hasArchive, ro);
    }


    @Override
    public DateTime extractCopyDateTime(OntModel model, ImmutableResearchObject researchObject) {
        Individual ro = model.getIndividual(researchObject.getUri().toString());
        Literal date = ro.getPropertyValue(ROEVO.archivedAtTime).asLiteral();
        return ISODateTimeFormat.dateTimeParser().parseDateTime(date.getString());
    }


    @Override
    public UserMetadata extractCopyAuthor(OntModel model, ImmutableResearchObject researchObject) {
        Individual ro = model.getIndividual(researchObject.getUri().toString());
        Individual author = ro.getPropertyResourceValue(ROEVO.archivedBy).as(Individual.class);
        String name;
        if (author.hasProperty(FOAF.name)) {
            name = author.getPropertyValue(FOAF.name).asLiteral().getString();
        } else {
            // backwards compatibility
            Model authorModel = researchObject.getBuilder().getDataset().getNamedModel(author.getURI());
            Resource author2 = authorModel.getResource(author.getURI());
            if (author2.hasProperty(FOAF.name)) {
                name = author2.getProperty(FOAF.name).getObject().asLiteral().getString();
            } else {
                name = author.getURI();
            }
        }
        return new UserMetadata(name, name, Role.AUTHENTICATED, URI.create(author.getURI()));
    }


    @Override
    public ResearchObject extractCopyOf(OntModel model, ImmutableResearchObject researchObject) {
        Individual ro = model.getIndividual(researchObject.getUri().toString());
        if (ro.hasProperty(ROEVO.isArchiveOf)) {
            Individual live = ro.getPropertyResourceValue(ROEVO.isArchiveOf).as(Individual.class);
            return ResearchObject.get(researchObject.getBuilder(), URI.create(live.getURI()));
        } else {
            return null;
        }
    }

}
