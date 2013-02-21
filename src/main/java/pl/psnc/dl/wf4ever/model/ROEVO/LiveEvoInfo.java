package pl.psnc.dl.wf4ever.model.ROEVO;

import java.net.URI;
import java.util.SortedSet;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.vocabulary.RO;
import pl.psnc.dl.wf4ever.vocabulary.ROEVO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;

public class LiveEvoInfo extends EvoInfo {

    /** Snapshots or archives of a Live RO, sorted from earliest to latest. */
    private SortedSet<ImmutableResearchObject> snapshotsOrArchives;


    public LiveEvoInfo(UserMetadata user, Dataset dataset, boolean useTransactions, ResearchObject researchObject,
            URI uri) {
        super(user, dataset, useTransactions, researchObject, uri);
    }


    public LiveEvoInfo(UserMetadata user, ResearchObject researchObject, URI uri) {
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
    public static LiveEvoInfo create(Builder builder, URI uri, ResearchObject researchObject) {
        LiveEvoInfo evoInfo = builder.buildLiveEvoInfo(uri, researchObject, builder.getUser(), DateTime.now());
        evoInfo.save();
        return evoInfo;
    }


    @Override
    public void save() {
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            Individual ro = model.createIndividual(getResearchObject().getUri().toString(), RO.ResearchObject);
            ro.addRDFType(ROEVO.LiveRO);
            for (ImmutableResearchObject immutableRO : snapshotsOrArchives) {
                immutableRO.getEvoBuilder().saveHasFrozen(model, immutableRO);
            }
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }


    public SortedSet<ImmutableResearchObject> getImmutableResearchObjects() {
        return snapshotsOrArchives;
    }


    @Override
    public void load() {
        // TODO Auto-generated method stub

    }

}
