package pl.psnc.dl.wf4ever.model.ROEVO;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.EvoBuilder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;
import pl.psnc.dl.wf4ever.vocabulary.PROV;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Resource;

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
     * @return a new manifest
     */
    public static ImmutableEvoInfo create(Builder builder, URI uri, ImmutableResearchObject researchObject) {
        ImmutableEvoInfo evoInfo = builder
                .buildImmutableEvoInfo(uri, researchObject, builder.getUser(), DateTime.now());
        evoInfo.save();
        return evoInfo;
    }


    @Override
    public ImmutableResearchObject getResearchObject() {
        return (ImmutableResearchObject) super.getResearchObject();
    }


    @Override
    public void save() {
        EvoBuilder builder = getResearchObject().getEvoBuilder();
        if (builder == null) {
            throw new IllegalStateException("The immutable research object has no evo builder set");
        }
        super.save();
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            builder.saveRDFType(model, getResearchObject());
            builder.saveFrozenAt(model, getResearchObject());
            builder.saveFrozenBy(model, getResearchObject());
            if (liveRO != null) {
                builder.saveHasLive(model, getResearchObject());
                previousRO = liveRO.getImmutableResearchObjects().last();
                liveRO.getImmutableResearchObjects().add(getResearchObject());
                liveRO.getLiveEvoInfo().save();
                liveRO.getLiveEvoInfo().serialize();
            }
            if (previousRO != null) {
                Individual ro = model.getIndividual(getResearchObject().getUri().toString());
                Resource prev = model.getResource(previousRO.getUri().toString());
                ro.addProperty(PROV.wasRevisionOf, prev);

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
        // TODO Auto-generated method stub

    }
}
