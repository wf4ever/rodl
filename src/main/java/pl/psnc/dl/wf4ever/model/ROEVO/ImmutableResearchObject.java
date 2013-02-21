package pl.psnc.dl.wf4ever.model.ROEVO;

import java.net.URI;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.common.db.EvoType;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.ArchiveBuilder;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.EvoBuilder;
import pl.psnc.dl.wf4ever.model.SnapshotBuilder;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.RO.Folder;
import pl.psnc.dl.wf4ever.model.RO.Manifest;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

import com.hp.hpl.jena.query.Dataset;

/**
 * An immutable a research object, i.e. a snapshot or an archive.
 * 
 * @author piotrekhol
 * 
 */
public class ImmutableResearchObject extends ResearchObject {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(ImmutableResearchObject.class);

    /** a writer of snapshot/archive properties. */
    private EvoBuilder evoBuilder;

    private ImmutableEvoInfo evoInfo;


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     * @param dataset
     *            custom dataset
     * @param useTransactions
     *            should transactions be used. Note that not using transactions on a dataset which already uses
     *            transactions may make it unreadable.
     * @param uri
     *            RO URI
     */
    public ImmutableResearchObject(UserMetadata user, Dataset dataset, boolean useTransactions, URI uri) {
        super(user, dataset, useTransactions, uri);
    }


    /**
     * Create a new immutable research object as a copy of a live one. Copies all aggregated resources, changes URIs in
     * annotation bodies.
     * 
     * @param uri
     *            URI of the copy
     * @param researchObject
     *            live research object
     * @param builder
     *            model instance builder
     * @param evoBuilder
     *            builder of evolution properties
     * @return the new research object
     */
    public static ImmutableResearchObject create(URI uri, ResearchObject researchObject, Builder builder,
            EvoBuilder evoBuilder) {
        if (ResearchObject.get(builder, uri) != null) {
            throw new ConflictException("Research Object already exists: " + uri);
        }
        ImmutableResearchObject immutableResearchObject = builder.buildImmutableResearchObject(uri,
            researchObject.getCreator(), researchObject.getCreated());
        immutableResearchObject.evoBuilder = evoBuilder;
        evoBuilder.setFrozenAt(immutableResearchObject, DateTime.now());
        evoBuilder.setFrozenBy(immutableResearchObject, builder.getUser());
        evoBuilder.setIsCopyOf(immutableResearchObject, researchObject);
        immutableResearchObject.copy(researchObject.getManifest(), evoBuilder);
        immutableResearchObject.save();
        // copy the ro:Resources
        for (pl.psnc.dl.wf4ever.model.RO.Resource resource : researchObject.getResources().values()) {
            try {
                immutableResearchObject.copy(resource, evoBuilder);
            } catch (BadRequestException e) {
                LOGGER.warn("Failed to copy the resource", e);
            }
        }
        //copy the annotations
        for (Annotation annotation : researchObject.getAnnotations().values()) {
            try {
                immutableResearchObject.copy(annotation, evoBuilder);
            } catch (BadRequestException e) {
                LOGGER.warn("Failed to copy the annotation", e);
            }
        }
        //copy the folders
        for (Folder folder : researchObject.getFolders().values()) {
            immutableResearchObject.copy(folder, evoBuilder);
        }
        return immutableResearchObject;
    }


    /**
     * Get a Research Object if it exists.
     * 
     * @param builder
     *            model instance builder
     * @param uri
     *            uri
     * @return an existing Research Object or null
     */
    public static ImmutableResearchObject get(Builder builder, URI uri) {
        ImmutableResearchObject researchObject = builder.buildImmutableResearchObject(uri);
        if (!researchObject.getManifest().isNamedGraph()) {
            return null;
        }
        switch (researchObject.getEvoType()) {
            case SNAPSHOT:
                researchObject.evoBuilder = new SnapshotBuilder();
                break;
            case ARCHIVE:
                researchObject.evoBuilder = new ArchiveBuilder();
                break;
            default:
                throw new IllegalStateException("New type must be a snaphot or archive");
        }
        return researchObject;
    }


    public EvoType getEvoType() {
        return getEvoInfo().getEvoType();
    }


    public ResearchObject getLiveRO() {
        return getImmutableEvoInfo().getLiveRO();
    }


    public void setLiveRO(ResearchObject liveRO) {
        getImmutableEvoInfo().setLiveRO(liveRO);
    }


    public ImmutableEvoInfo getImmutableEvoInfo() {
        if (evoInfo == null) {
            evoInfo = ImmutableEvoInfo.create(builder, getFixedEvolutionAnnotationBodyUri(), this);
            getAggregatedResources().put(evoInfo.getUri(), evoInfo);
            evoInfo.load();
        }
        return evoInfo;
    }


    @Override
    public EvoInfo getEvoInfo() {
        return getImmutableEvoInfo();
    }


    /**
     * Generate new evolution information, including the evolution annotation. This method will change the live RO evo
     * info only if this property is set (it isn't for not-finalized snapshots/archives).
     */
    @Override
    public void createEvoInfo() {
        try {
            evoInfo = ImmutableEvoInfo.create(builder, getFixedEvolutionAnnotationBodyUri(), this);
            getAggregatedResources().put(evoInfo.getUri(), evoInfo);
            evoInfo.save();
            evoInfo.serialize();

            this.evoInfoAnnotation = annotate(evoInfo.getUri(), this);
            this.getManifest().serialize();
        } catch (BadRequestException e) {
            LOGGER.error("Failed to create the evo info annotation", e);
        }
    }


    public EvoBuilder getEvoBuilder() {
        return evoBuilder;
    }


    public void copy(Manifest manifest, EvoBuilder evoBuilder) {
        manifest = manifest.copy(builder, this);
    }

}
