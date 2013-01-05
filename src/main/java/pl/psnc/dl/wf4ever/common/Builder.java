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
     * @param useTransanctions
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


    public UserMetadata getUser() {
        return user;
    }


    public Thing buildThing(URI uri) {
        Thing thing = new Thing(user, dataset, useTransactions, uri);
        thing.setBuilder(this);
        return thing;
    }


    public ResearchObject buildResearchObject(URI uri, URI creator, DateTime created) {
        ResearchObject researchObject = new ResearchObject(user, dataset, useTransactions, uri);
        researchObject.setCreator(creator);
        researchObject.setCreated(created);
        researchObject.setBuilder(this);
        return researchObject;
    }


    public ResearchObject buildResearchObject(URI uri) {
        ResearchObject researchObject = new ResearchObject(user, dataset, useTransactions, uri);
        researchObject.setBuilder(this);
        return researchObject;
    }


    public SnapshotResearchObject buildSnapshotResearchObject(URI uri, ResearchObject liveRO) {
        SnapshotResearchObject snapshot = new SnapshotResearchObject(user, dataset, useTransactions, uri, liveRO);
        snapshot.setBuilder(this);
        return snapshot;
    }


    public ArchiveResearchObject buildArchiveResearchObject(URI uri, ResearchObject liveRO) {
        ArchiveResearchObject archive = new ArchiveResearchObject(user, dataset, useTransactions, uri, liveRO);
        archive.setBuilder(this);
        return archive;
    }


    public Manifest buildManifest(URI uri, ResearchObject researchObject) {
        Manifest manifest = new Manifest(user, dataset, useTransactions, uri, researchObject);
        manifest.setBuilder(this);
        return manifest;
    }


    public Manifest buildManifest(URI uri, ResearchObject researchObject, URI creator, DateTime created) {
        Manifest manifest = new Manifest(user, dataset, useTransactions, uri, researchObject);
        manifest.setCreator(creator);
        manifest.setCreated(created);
        manifest.setBuilder(this);
        return manifest;
    }


    public AggregatedResource buildAggregatedResource(URI uri, ResearchObject researchObject, URI creator,
            DateTime created) {
        AggregatedResource resource = new AggregatedResource(user, dataset, useTransactions, researchObject, uri);
        resource.setCreator(creator);
        resource.setCreated(created);
        resource.setBuilder(this);
        return resource;
    }


    public Proxy buildProxy(URI uri, AggregatedResource proxyFor, Aggregation proxyIn) {
        Proxy proxy = new Proxy(user, dataset, useTransactions, uri);
        proxy.setProxyFor(proxyFor);
        proxy.setProxyIn(proxyIn);
        proxy.setBuilder(this);
        return proxy;
    }


    public Annotation buildAnnotation(ResearchObject researchObject, URI uri, Thing body, Set<Thing> targets) {
        Annotation annotation = new Annotation(user, dataset, useTransactions, researchObject, uri);
        annotation.setBody(body);
        annotation.setAnnotated(targets);
        annotation.setBuilder(this);
        return annotation;
    }


    public Annotation buildAnnotation(ResearchObject researchObject, URI uri, Thing body, Set<Thing> targets,
            URI creator, DateTime created) {
        Annotation annotation = buildAnnotation(researchObject, uri, body, targets);
        annotation.setCreator(creator);
        annotation.setCreated(created);
        return annotation;
    }


    public Annotation buildAnnotation(ResearchObject researchObject, URI uri, Thing body, Thing target, URI creator,
            DateTime created) {
        Set<Thing> targets = new HashSet<>();
        targets.add(target);
        return buildAnnotation(researchObject, uri, body, targets, creator, created);
    }


    public Resource buildResource(ResearchObject researchObject, URI uri, URI creator, DateTime created) {
        Resource resource = new Resource(user, dataset, useTransactions, researchObject, uri);
        resource.setCreator(creator);
        resource.setCreated(created);
        resource.setBuilder(this);
        return resource;
    }


    public Folder buildFolder(ResearchObject researchObject, URI uri, URI creator, DateTime created) {
        Folder folder = new Folder(user, dataset, useTransactions, researchObject, uri);
        folder.setCreator(creator);
        folder.setCreated(created);
        folder.setBuilder(this);
        return folder;
    }


    public FolderResourceMap buildFolderResourceMap(URI uri, Folder folder) {
        FolderResourceMap map = new FolderResourceMap(user, dataset, useTransactions, folder, uri);
        return map;
    }


    public FolderEntry buildFolderEntry(URI uri, AggregatedResource aggregatedResource, Folder proxyIn, String name) {
        FolderEntry entry = new FolderEntry(user, dataset, useTransactions, uri);
        entry.setProxyFor(aggregatedResource);
        entry.setProxyIn(proxyIn);
        entry.setEntryName(name);
        return entry;
    }
}
