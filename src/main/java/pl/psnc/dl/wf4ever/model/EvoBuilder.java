package pl.psnc.dl.wf4ever.model;

import java.net.URI;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.evo.EvoType;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.model.ROEVO.ImmutableResearchObject;
import pl.psnc.dl.wf4ever.vocabulary.PROV;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Builder saving snapshot or archival metadata in resources.
 * 
 * Snapshot or archival operations are called "freeze" for simplicity.
 * 
 * @author piotrekhol
 * 
 */
public abstract class EvoBuilder {

    public static EvoBuilder get(EvoType evoType) {
        if (evoType == null) {
            throw new NullPointerException("Null evolution type");
        }
        switch (evoType) {
            case SNAPSHOT:
                return new SnapshotBuilder();
            case ARCHIVE:
                return new ArchiveBuilder();
            default:
                throw new IllegalArgumentException("Unsupported evolution type: " + evoType);
        }
    }


    public abstract void saveRDFType(OntModel model, ImmutableResearchObject researchObject);


    public abstract void saveHasLive(OntModel model, ImmutableResearchObject researchObject);


    public abstract void saveCopyDateTime(OntModel model, ImmutableResearchObject researchObject);


    public abstract void saveCopyAuthor(OntModel model, ImmutableResearchObject researchObject);


    public abstract void saveHasCopy(OntModel model, ImmutableResearchObject researchObject);


    public void saveHasPrevious(OntModel model, ImmutableResearchObject researchObject,
            ImmutableResearchObject previousRO) {
        Individual ro = model.getIndividual(researchObject.getUri().toString());
        Resource prev = model.getResource(previousRO.getUri().toString());
        ro.addProperty(PROV.wasRevisionOf, prev);
    }


    public abstract DateTime extractCopyDateTime(OntModel model, ImmutableResearchObject researchObject);


    public abstract UserMetadata extractCopyAuthor(OntModel model, ImmutableResearchObject researchObject);


    public abstract ResearchObject extractCopyOf(OntModel model, ImmutableResearchObject researchObject);


    public ImmutableResearchObject extractPreviousRO(OntModel model, ImmutableResearchObject researchObject) {
        Individual ro = model.getIndividual(researchObject.getUri().toString());
        if (ro.hasProperty(PROV.wasRevisionOf)) {
            Individual prev = ro.getPropertyResourceValue(PROV.wasRevisionOf).as(Individual.class);
            return ImmutableResearchObject.get(researchObject.getBuilder(), URI.create(prev.getURI()));
        } else {
            return null;
        }
    }

}
