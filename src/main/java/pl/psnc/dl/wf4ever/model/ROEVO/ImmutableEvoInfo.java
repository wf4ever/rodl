package pl.psnc.dl.wf4ever.model.ROEVO;

import java.net.URI;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.evo.EvoType;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.EvoBuilder;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.model.ROEVO.Change.ChangeType;
import pl.psnc.dl.wf4ever.vocabulary.RO;
import pl.psnc.dl.wf4ever.vocabulary.ROEVO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Evolution information resource associated with an immutable RO such as a snapshot or an archive.
 * 
 * @author piotrekhol
 * 
 */
public class ImmutableEvoInfo extends EvoInfo {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(ImmutableEvoInfo.class);
    /** previous snapshot or archive, if exists. */
    private ImmutableResearchObject previousRO;

    /** Change specification if there is a previous RO. */
    private ChangeSpecification changeSpecification;

    /** Has the RO been finalized, i.e. is it really immutable. */
    private boolean finalized = false;

    /** Temporary property URI for marking if the RO has been finalized. It should go to rodl-common. */
    private static final String IS_FINALIZED = ROEVO.NAMESPACE + "isFinalized";


    /**
     * Constructor.
     * 
     * @param user
     *            user creating this resource
     * @param dataset
     *            custom dataset
     * @param useTransactions
     *            should transactions be used. Note that not using transactions on a dataset which already uses
     *            transactions may make it unreadable.
     * @param researchObject
     *            research object described by this resource
     * @param uri
     *            resource URI
     */
    public ImmutableEvoInfo(UserMetadata user, Dataset dataset, boolean useTransactions, ResearchObject researchObject,
            URI uri) {
        super(user, dataset, useTransactions, researchObject, uri);
    }


    /**
     * Create and save a new evolution information. Add it to the RO properties.
     * 
     * @param builder
     *            model instance builder
     * @param uri
     *            manifest URI
     * @param researchObject
     *            research object that is described
     * @param evoType
     *            is the research object a snapshot or an archive
     * @return a new manifest
     */
    public static ImmutableEvoInfo create(Builder builder, URI uri, ImmutableResearchObject researchObject,
            EvoType evoType) {
        ImmutableEvoInfo evoInfo = builder.buildImmutableEvoInfo(uri, researchObject, builder.getUser(),
            DateTime.now(), evoType);
        evoInfo.save();
        evoInfo.scheduleToSerialize(researchObject.getUri(), RDFFormat.TURTLE);
        researchObject.getAggregatedResources().put(evoInfo.getUri(), evoInfo);
        return evoInfo;
    }


    /**
     * Return an evolution information resource or null if not found in the triplestore.
     * 
     * @param builder
     *            model instance builder
     * @param uri
     *            evolution information resource URI
     * @param researchObject
     *            research object which this evo info should describe
     * @return an existing evo info or null
     */
    public static ImmutableEvoInfo get(Builder builder, URI uri, ImmutableResearchObject researchObject) {
        ImmutableEvoInfo evoInfo = builder.buildImmutableEvoInfo(uri, researchObject);
        if (!evoInfo.isNamedGraph()) {
            return null;
        }
        researchObject.getAggregatedResources().put(evoInfo.getUri(), evoInfo);
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
            } else {
                ro.removeAll(model.createProperty(IS_FINALIZED));
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
                //the change specification is rebuilt on every save
                if (changeSpecification != null) {
                    changeSpecification.delete();
                }
                changeSpecification = ChangeSpecification.create(getBuilder(), getResearchObject());
                changeSpecification.createChanges(previousRO);
            }
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }


    /**
     * Save a change specification in this resource's model in the triplestore.
     * 
     * @param changeSpecification
     *            the change specification
     */
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


    /**
     * Save a change in this resource's model in the triplestore.
     * 
     * @param change
     *            the change, i.e. a resource addition, modification or removal
     */
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
            changeSpecification = extractChangeSpecification();
        } finally {
            endTransaction(transactionStarted);
        }
    }


    /**
     * Create a change specification based on the data saved in this resource's model.
     * 
     * @return a change specification with changes, or null if not found
     */
    private ChangeSpecification extractChangeSpecification() {
        boolean transactionStarted = beginTransaction(ReadWrite.READ);
        try {
            Individual ro = model.getIndividual(getResearchObject().getUri().toString());
            Resource cs = ro.getPropertyResourceValue(ROEVO.wasChangedBy);
            if (cs == null) {
                return null;
            }
            changeSpecification = getBuilder().buildChangeSpecification(URI.create(cs.getURI()), getResearchObject());
            extractChanges(changeSpecification, ChangeType.ADDITION, ROEVO.Addition);
            extractChanges(changeSpecification, ChangeType.MODIFICATION, ROEVO.Modification);
            extractChanges(changeSpecification, ChangeType.REMOVAL, ROEVO.Removal);
            return changeSpecification;
        } finally {
            endTransaction(transactionStarted);
        }
    }


    /**
     * Create changes of a given type based on this resource's model.
     * 
     * @param changeSpecification
     *            change specification to which the changes must belong
     * @param type
     *            type of change to create
     * @param typeClass
     *            class of change to look for
     */
    private void extractChanges(ChangeSpecification changeSpecification, ChangeType type, OntClass typeClass) {
        String queryString = String
                .format(
                    "PREFIX roevo: <%s> SELECT ?change ?resource WHERE { <%s> roevo:hasChange ?change . ?change a roevo:%s ; roevo:relatedResource ?resource . }",
                    ROEVO.NAMESPACE, changeSpecification.getUri().toString(), typeClass.getLocalName());

        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        try {
            ResultSet results = qe.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                RDFNode c = solution.get("change");
                URI cUri = URI.create(c.asResource().getURI());
                RDFNode r = solution.get("resource");
                URI rUri = URI.create(r.asResource().getURI());
                AggregatedResource resource;
                if (getResearchObject().getAggregatedResources().containsKey(rUri)) {
                    resource = getResearchObject().getAggregatedResources().get(rUri);
                } else if (getPreviousRO().getAggregatedResources().containsKey(rUri)) {
                    resource = getPreviousRO().getAggregatedResources().get(rUri);
                } else {
                    resource = null;
                }
                Change change = getBuilder().buildChange(cUri, changeSpecification, resource, type);
                changeSpecification.getChanges().add(change);
            }
        } finally {
            qe.close();
        }
    }


    public boolean isFinalized() {
        return finalized;
    }


    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }

}
