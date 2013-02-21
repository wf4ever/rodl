package pl.psnc.dl.wf4ever.model.ROEVO;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.joda.time.DateTime;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.common.db.EvoType;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.EvoBuilder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;
import pl.psnc.dl.wf4ever.vocabulary.RO;
import pl.psnc.dl.wf4ever.vocabulary.ROEVO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;

public class ImmutableEvoInfo extends EvoInfo {

    /** the research object that is snapshotted/archived. */
    private ResearchObject liveRO;

    /** previous snapshot or archive, if exists. */
    private ImmutableResearchObject previousRO;


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
            model.createIndividual(getResearchObject().getUri().toString(), RO.ResearchObject);
            builder.saveRDFType(model, getResearchObject());
            builder.saveCopyDateTime(model, getResearchObject());
            builder.saveCopyAuthor(model, getResearchObject());
            if (liveRO != null) {
                builder.saveHasLive(model, getResearchObject());
                previousRO = liveRO.getImmutableResearchObjects().isEmpty() ? null : liveRO
                        .getImmutableResearchObjects().last();
                liveRO.getImmutableResearchObjects().add(getResearchObject());
                liveRO.getLiveEvoInfo().save();
            }
            if (previousRO != null) {
                builder.saveHasPrevious(model, getResearchObject(), previousRO);

                //FIXME only this remains to refactor
                try {
                    ROSRService.SMS.get().storeAggregatedDifferences(getResearchObject(), previousRO);
                } catch (URISyntaxException | IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
        serialize(uri, RDFFormat.TURTLE);
    }


    public ResearchObject getLiveRO() {
        return liveRO;
    }


    public void setLiveRO(ResearchObject liveRO) {
        this.liveRO = liveRO;
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
            EvoBuilder builder = EvoBuilder.get(evoType);
            getResearchObject().setCopyDateTime(builder.extractCopyDateTime(model, getResearchObject()));
            getResearchObject().setCopyAuthor(builder.extractCopyAuthor(model, getResearchObject()));
            getResearchObject().setCopyOf(builder.extractCopyOf(model, getResearchObject()));
            liveRO = (ResearchObject) getResearchObject().getCopyOf();
            previousRO = builder.extractPreviousRO(model, getResearchObject());
        } finally {
            endTransaction(transactionStarted);
        }
    }

}
