package pl.psnc.dl.wf4ever.model.ROEVO;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.evo.EvoType;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.EvoBuilder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.vocabulary.RO;
import pl.psnc.dl.wf4ever.vocabulary.ROEVO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class LiveEvoInfo extends EvoInfo {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(LiveEvoInfo.class);

    /** Snapshots or archives of a Live RO, sorted from earliest to most recent. */
    private SortedSet<ImmutableResearchObject> snapshotsOrArchives = new TreeSet<>();


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
        super.save();
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            Individual ro = model.createIndividual(getResearchObject().getUri().toString(), RO.ResearchObject);
            ro.addRDFType(ROEVO.LiveRO);
            for (ImmutableResearchObject immutableRO : snapshotsOrArchives) {
                EvoBuilder builder = EvoBuilder.get(immutableRO.getEvoType());
                builder.saveHasCopy(model, immutableRO);
                builder.saveCopyDateTime(model, immutableRO);
                builder.saveCopyAuthor(model, immutableRO);
            }
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
        serialize(uri, RDFFormat.TURTLE);
    }


    public SortedSet<ImmutableResearchObject> getImmutableResearchObjects() {
        return snapshotsOrArchives;
    }


    @Override
    public void load() {
        boolean transactionStarted = beginTransaction(ReadWrite.READ);
        try {
            Individual ro = model.getIndividual(getResearchObject().getUri().toString());
            if (ro.hasRDFType(ROEVO.LiveRO)) {
                evoType = EvoType.LIVE;
            } else {
                LOGGER.warn("Evo info has no information about the Live class of this RO: " + getResearchObject());
                evoType = EvoType.LIVE;
            }
            Set<RDFNode> snapshots = ro.listPropertyValues(ROEVO.hasSnapshot).toSet();
            Set<RDFNode> archives = ro.listPropertyValues(ROEVO.hasArchive).toSet();
            Set<RDFNode> immutables = new HashSet<>();
            immutables.addAll(snapshots);
            immutables.addAll(archives);
            for (RDFNode node : immutables) {
                ImmutableResearchObject immutable = ImmutableResearchObject.get(builder,
                    URI.create(node.asResource().getURI()));
                if (immutable == null) {
                    LOGGER.warn("Immutable research object does not exist: " + node.asResource().getURI());
                } else {
                    snapshotsOrArchives.add(immutable);
                }
            }
        } finally {
            endTransaction(transactionStarted);
        }
    }
}
