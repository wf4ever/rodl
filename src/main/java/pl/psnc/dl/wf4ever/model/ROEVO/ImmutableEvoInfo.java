package pl.psnc.dl.wf4ever.model.ROEVO;

import java.net.URI;

import org.joda.time.DateTime;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.common.db.EvoType;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.EvoBuilder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.vocabulary.RO;
import pl.psnc.dl.wf4ever.vocabulary.ROEVO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Resource;

public class ImmutableEvoInfo extends EvoInfo {

    /** previous snapshot or archive, if exists. */
    private ImmutableResearchObject previousRO;

    /** Has the RO been finalized, i.e. is it really immutable. */
    private boolean finalized = false;

    /** Temporary property URI for marking if the RO has been finalized. It should go to rodl-common. */
    private static final String IS_FINALIZED = ROEVO.NAMESPACE + "isFinalized";


    public ImmutableEvoInfo(UserMetadata user, Dataset dataset, boolean useTransactions, ResearchObject researchObject,
            URI uri) {
        super(user, dataset, useTransactions, researchObject, uri);
    }


    public ImmutableEvoInfo(UserMetadata user, ResearchObject researchObject, URI uri) {
        super(user, researchObject, uri);
    }


    /**
     * Create and save a new manifest.
     * 
     * @param builder
     *            model instance builder
     * @param uri
     *            manifest URI
     * @param researchObject
     *            research object that is described
     * @param evoType
     * @return a new manifest
     */
    public static ImmutableEvoInfo create(Builder builder, URI uri, ImmutableResearchObject researchObject,
            EvoType evoType) {
        ImmutableEvoInfo evoInfo = builder.buildImmutableEvoInfo(uri, researchObject, builder.getUser(),
            DateTime.now(), evoType);
        evoInfo.save();
        return evoInfo;
    }


    @Override
    public ImmutableResearchObject getResearchObject() {
        return (ImmutableResearchObject) super.getResearchObject();
    }


    @Override
    public void save() {
        super.save();
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            EvoBuilder builder = EvoBuilder.get(evoType);
            Individual ro = model.createIndividual(getResearchObject().getUri().toString(), RO.ResearchObject);
            builder.saveRDFType(model, getResearchObject());
            builder.saveCopyDateTime(model, getResearchObject());
            builder.saveCopyAuthor(model, getResearchObject());
            if (!finalized) {
                ro.addProperty(model.createProperty(IS_FINALIZED), "false");
            }

            ResearchObject liveRO = getResearchObject().getLiveRO();
            if (liveRO != null) {
                builder.saveHasLive(model, getResearchObject());
                previousRO = liveRO.getImmutableResearchObjects().isEmpty() ? null : liveRO
                        .getImmutableResearchObjects().last();
                if (finalized) {
                    liveRO.getImmutableResearchObjects().add(getResearchObject());
                    liveRO.getLiveEvoInfo().save();
                }
            }
            if (previousRO != null) {
                builder.saveHasPrevious(model, getResearchObject(), previousRO);
                //TODO move the store differences code here
                ChangeSpecification cs = ChangeSpecification.create(getBuilder(), getResearchObject());
                cs.findChanges(previousRO);
            }
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
        //        if (previousRO != null) {
        //
        //            //FIXME only this remains to refactor
        //            try {
        //                ROSRService.SMS.get().storeAggregatedDifferences(getResearchObject(), previousRO);
        //            } catch (URISyntaxException | IOException e) {
        //                // TODO Auto-generated catch block
        //                e.printStackTrace();
        //            }
        //        }
        serialize(uri, RDFFormat.TURTLE);
    }


    public void saveChangeSpecification(ChangeSpecification changeSpecification) {
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            Individual cs = model.createIndividual(changeSpecification.getUri().toString(), ROEVO.ChangeSpecification);
            Individual ro = model.getIndividual(getResearchObject().getUri().toString());
            ro.addProperty(ROEVO.wasChangedBy, cs);
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }


    public void saveChange(Change change) {
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            Individual cs = model.createIndividual(change.getChangeSpecification().getUri().toString(),
                ROEVO.ChangeSpecification);
            Individual ch = model.createIndividual(change.getUri().toString(), ROEVO.Change);
            cs.addProperty(ROEVO.hasChange, ch);
            switch (change.getChangeType()) {
                case ADDITION:
                    ch.addRDFType(ROEVO.Addition);
                    break;
                case REMOVAL:
                    ch.addRDFType(ROEVO.Removal);
                    break;
                case MODIFICATION:
                    ch.addRDFType(ROEVO.Modification);
                    break;
                default:
            }
            Resource resource = model.createResource(change.getResource().getUri().toString());
            ch.addProperty(ROEVO.relatedResource, resource);
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }


    public ImmutableResearchObject getPreviousRO() {
        return previousRO;
    }


    @Override
    public void load() {
        boolean transactionStarted = beginTransaction(ReadWrite.READ);
        try {
            Individual ro = model.getIndividual(getResearchObject().getUri().toString());
            if (ro.hasRDFType(ROEVO.SnapshotRO)) {
                evoType = EvoType.SNAPSHOT;
            } else if (ro.hasRDFType(ROEVO.ArchivedRO)) {
                evoType = EvoType.ARCHIVE;
            } else {
                throw new IllegalStateException("Evo info has no information about the evolution class of this RO: "
                        + getResearchObject());
            }
            if (ro.hasProperty(model.createProperty(IS_FINALIZED))) {
                finalized = ro.getPropertyValue(model.createProperty(IS_FINALIZED)).asLiteral().getBoolean();
            } else {
                // if no value found, it must have been finalized
                finalized = true;
            }
            EvoBuilder builder = EvoBuilder.get(evoType);
            getResearchObject().setCopyDateTime(builder.extractCopyDateTime(model, getResearchObject()));
            getResearchObject().setCopyAuthor(builder.extractCopyAuthor(model, getResearchObject()));
            getResearchObject().setCopyOf(builder.extractCopyOf(model, getResearchObject()));
            previousRO = builder.extractPreviousRO(model, getResearchObject());
        } finally {
            endTransaction(transactionStarted);
        }
    }


    public boolean isFinalized() {
        return finalized;
    }


    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }

}
