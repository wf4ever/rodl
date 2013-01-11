package pl.psnc.dl.wf4ever.common;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.ORE.Aggregation;
import pl.psnc.dl.wf4ever.model.ORE.Proxy;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.Folder;
import pl.psnc.dl.wf4ever.model.RO.FolderEntry;
import pl.psnc.dl.wf4ever.model.RO.FolderResourceMap;
import pl.psnc.dl.wf4ever.model.RO.Manifest;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.model.RO.Resource;
import pl.psnc.dl.wf4ever.model.ROEVO.ArchiveResearchObject;
import pl.psnc.dl.wf4ever.model.ROEVO.SnapshotResearchObject;

import com.hp.hpl.jena.query.Dataset;

/**
 * Builder in the builder design pattern. Used to build ROSR and EVO model instances.
 * 
 * @author piotrekhol
 * 
 */
public class Builder {

    /** Jena dataset. */
    private final Dataset dataset;

    /** Use transactions on the Jena dataset. */
    private final boolean useTransactions;

    /** Authenticated user. */
    private final UserMetadata user;


    /**
     * Constructor.
     * 
     * @param user
     *            Authenticated user
     * @param dataset
     *            Jena dataset
     * @param useTransactions
     *            Use transactions on the Jena dataset
     */
    public Builder(UserMetadata user, Dataset dataset, boolean useTransactions) {
        this.dataset = dataset;
        this.user = user;
        this.useTransactions = useTransactions;
    }


    /**
     * Constructor of a builder that uses the default dataset with transactions.
     * 
     * @param user
     *            Authenticated user
     */
    public Builder(UserMetadata user) {
        this(user, null, true);
    }


    public Dataset getDataset() {
        return dataset;
    }


    public boolean isUseTransactions() {
        return useTransactions;
    }


    public UserMetadata getUser() {
        return user;
    }


    /**
     * Build a new Thing.
     * 
     * @param uri
     *            the URI
     * @return a new Thing instance
     */
    public Thing buildThing(URI uri) {
        Thing thing = new Thing(user, dataset, useTransactions, uri);
        thing.setBuilder(this);
        return thing;
    }


    /**
     * Build a new research object.
     * 
     * @param uri
     *            the URI
     * @param creator
     *            author
     * @param created
     *            creation date
     * @return a new Research Object instance
     */
    public ResearchObject buildResearchObject(URI uri, UserMetadata creator, DateTime created) {
        ResearchObject researchObject = new ResearchObject(user, dataset, useTransactions, uri);
        researchObject.setCreator(creator);
        researchObject.setCreated(created);
        researchObject.setBuilder(this);
        return researchObject;
    }


    /**
     * Build a new research object.
     * 
     * @param uri
     *            the URI
     * @return a new Research Object instance
     */
    public ResearchObject buildResearchObject(URI uri) {
        ResearchObject researchObject = new ResearchObject(user, dataset, useTransactions, uri);
        researchObject.setBuilder(this);
        return researchObject;
    }


    /**
     * Build a new snapshot research object.
     * 
     * @param uri
     *            the URI
     * @param liveRO
     *            the research object that is snapshotted
     * @return a new snapshot Research Object instance
     */
    public SnapshotResearchObject buildSnapshotResearchObject(URI uri, ResearchObject liveRO) {
        SnapshotResearchObject snapshot = new SnapshotResearchObject(user, dataset, useTransactions, uri, liveRO);
        snapshot.setBuilder(this);
        return snapshot;
    }


    /**
     * Build a new archive research object.
     * 
     * @param uri
     *            the URI
     * @param liveRO
     *            the research object that is archived
     * @return a new archive Research Object instance
     */
    public ArchiveResearchObject buildArchiveResearchObject(URI uri, ResearchObject liveRO) {
        ArchiveResearchObject archive = new ArchiveResearchObject(user, dataset, useTransactions, uri, liveRO);
        archive.setBuilder(this);
        return archive;
    }


    /**
     * Build a new manifest.
     * 
     * @param uri
     *            the URI
     * @param researchObject
     *            the research object that is described
     * @return a new manifest instance
     */
    public Manifest buildManifest(URI uri, ResearchObject researchObject) {
        Manifest manifest = new Manifest(user, dataset, useTransactions, uri, researchObject);
        manifest.setBuilder(this);
        return manifest;
    }


    /**
     * Build a new manifest.
     * 
     * @param uri
     *            the URI
     * @param researchObject
     *            the research object that is described
     * @param creator
     *            author
     * @param created
     *            creation date
     * @return a new manifest instance
     */
    public Manifest buildManifest(URI uri, ResearchObject researchObject, UserMetadata creator, DateTime created) {
        Manifest manifest = new Manifest(user, dataset, useTransactions, uri, researchObject);
        manifest.setCreator(creator);
        manifest.setCreated(created);
        manifest.setBuilder(this);
        return manifest;
    }


    /**
     * Build a new aggregated resource.
     * 
     * @param uri
     *            the URI
     * @param researchObject
     *            the research object that aggregates the resource
     * @param creator
     *            author
     * @param created
     *            creation date
     * @return a new aggregated resource instance
     */
    public AggregatedResource buildAggregatedResource(URI uri, ResearchObject researchObject, UserMetadata creator,
            DateTime created) {
        AggregatedResource resource = new AggregatedResource(user, dataset, useTransactions, researchObject, uri);
        resource.setCreator(creator);
        resource.setCreated(created);
        resource.setBuilder(this);
        return resource;
    }


    /**
     * Build a new proxy.
     * 
     * @param uri
     *            the URI
     * @param proxyFor
     *            the resource for which the proxy stands
     * @param proxyIn
     *            the aggregation aggregating the proxyFor resource
     * @return a new proxy
     */
    public Proxy buildProxy(URI uri, AggregatedResource proxyFor, Aggregation proxyIn) {
        Proxy proxy = new Proxy(user, dataset, useTransactions, uri);
        proxy.setProxyFor(proxyFor);
        proxy.setProxyIn(proxyIn);
        proxy.setBuilder(this);
        return proxy;
    }


    /**
     * Build a new annotation.
     * 
     * @param researchObject
     *            research object aggregating the annotation
     * @param uri
     *            annotation URI
     * @param bodyUri
     *            annotation body URI
     * @param targets
     *            annotated resources
     * @return a new annotation
     */
    public Annotation buildAnnotation(ResearchObject researchObject, URI uri, URI bodyUri, Set<Thing> targets) {
        Annotation annotation = new Annotation(user, dataset, useTransactions, researchObject, uri);
        annotation.setBodyUri(bodyUri);
        annotation.setAnnotated(targets);
        annotation.setBuilder(this);
        return annotation;
    }


    /**
     * Build a new annotation.
     * 
     * @param researchObject
     *            research object aggregating the annotation
     * @param uri
     *            annotation URI
     * @param bodyUri
     *            annotation body URI
     * @param targets
     *            annotated resources
     * @param creator
     *            author
     * @param created
     *            creation date
     * @return a new annotation
     */
    public Annotation buildAnnotation(ResearchObject researchObject, URI uri, URI bodyUri, Set<Thing> targets,
            UserMetadata creator, DateTime created) {
        Annotation annotation = buildAnnotation(researchObject, uri, bodyUri, targets);
        annotation.setCreator(creator);
        annotation.setCreated(created);
        annotation.setBuilder(this);
        return annotation;
    }


    /**
     * Build a new annotation with one annotated resource.
     * 
     * @param researchObject
     *            research object aggregating the annotation
     * @param uri
     *            annotation URI
     * @param bodyUri
     *            annotation body URI
     * @param target
     *            the annotated resource
     * @param creator
     *            author
     * @param created
     *            creation date
     * @return a new annotation
     */
    public Annotation buildAnnotation(ResearchObject researchObject, URI uri, URI bodyUri, Thing target,
            UserMetadata creator, DateTime created) {
        Set<Thing> targets = new HashSet<>();
        targets.add(target);
        return buildAnnotation(researchObject, uri, bodyUri, targets, creator, created);
    }


    /**
     * Build a new ro:Resource.
     * 
     * @param researchObject
     *            research object aggregating the resource
     * @param uri
     *            resource URI
     * @param creator
     *            author
     * @param created
     *            creation date
     * @return a new resource
     */
    public Resource buildResource(ResearchObject researchObject, URI uri, UserMetadata creator, DateTime created) {
        Resource resource = new Resource(user, dataset, useTransactions, researchObject, uri);
        resource.setCreator(creator);
        resource.setCreated(created);
        resource.setBuilder(this);
        return resource;
    }


    /**
     * Build a new folder.
     * 
     * @param researchObject
     *            research object aggregating the folder
     * @param uri
     *            folder URI
     * @param creator
     *            author
     * @param created
     *            creation date
     * @return a new folder
     */
    public Folder buildFolder(ResearchObject researchObject, URI uri, UserMetadata creator, DateTime created) {
        Folder folder = new Folder(user, dataset, useTransactions, researchObject, uri);
        folder.setCreator(creator);
        folder.setCreated(created);
        folder.setBuilder(this);
        return folder;
    }


    /**
     * Build a new folder resource map.
     * 
     * @param uri
     *            resource map URI
     * @param folder
     *            folder it describes
     * @return a new folder resource map
     */
    public FolderResourceMap buildFolderResourceMap(URI uri, Folder folder) {
        FolderResourceMap map = new FolderResourceMap(user, dataset, useTransactions, folder, uri);
        map.setBuilder(this);
        return map;
    }


    /**
     * Build a new folder entry.
     * 
     * @param uri
     *            folder entry URI
     * @param aggregatedResource
     *            resource it stands for
     * @param proxyIn
     *            folder for which it is created
     * @param name
     *            resource name in the folder
     * @return a new folder entry
     */
    public FolderEntry buildFolderEntry(URI uri, AggregatedResource aggregatedResource, Folder proxyIn, String name) {
        FolderEntry entry = new FolderEntry(user, dataset, useTransactions, uri);
        entry.setProxyFor(aggregatedResource);
        entry.setProxyIn(proxyIn);
        entry.setEntryName(name);
        entry.setBuilder(this);
        return entry;
    }
}
