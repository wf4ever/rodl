package pl.psnc.dl.wf4ever.model.RO;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.common.Builder;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.ORE.Aggregation;
import pl.psnc.dl.wf4ever.model.ORE.Proxy;
import pl.psnc.dl.wf4ever.vocabulary.ORE;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * ro:Folder.
 * 
 * @author piotrekhol
 * 
 */
public class Folder extends Resource implements Aggregation {

    /** folder entries. */
    private Set<FolderEntry> folderEntries;

    /** Resource map (graph with folder description) URI. */
    private FolderResourceMap resourceMap;

    /** has the resource map been loaded. */
    private boolean loaded;

    /** is the folder a root folder in the RO. */
    private boolean rootFolder;


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     * @param researchObject
     *            The RO it is aggregated by
     * @param uri
     *            resource URI
     */
    public Folder(UserMetadata user, Dataset dataset, boolean useTransactions, ResearchObject researchObject, URI uri) {
        super(user, dataset, useTransactions, researchObject, uri);
        this.loaded = false;
    }


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     * @param researchObject
     *            The RO it is aggregated by
     * @param uri
     *            resource URI
     */
    public Folder(UserMetadata user, ResearchObject researchObject, URI uri) {
        super(user, researchObject, uri);
        this.loaded = false;
    }


    public Set<FolderEntry> getFolderEntries() {
        return folderEntries;
    }


    public void setFolderEntries(Set<FolderEntry> folderEntries) {
        this.folderEntries = folderEntries;
    }


    public FolderResourceMap getResourceMap()
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        if (!loaded) {
            load();
        }
        return resourceMap;
    }


    public boolean isLoaded() {
        return loaded;
    }


    public boolean isRootFolder() {
        return rootFolder;
    }


    public void setRootFolder(boolean rootFolder) {
        this.rootFolder = rootFolder;
    }


    public static Folder get(Builder builder, ResearchObject researchObject, URI uri, URI creator, DateTime created) {
        Folder folder = builder.buildFolder(researchObject, uri, creator, created);
        return folder;
    }


    public void load()
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        URI resourceMapUri = FolderResourceMap.generateResourceMapUri(this);
        resourceMap = builder.buildFolderResourceMap(resourceMapUri, this);
        folderEntries = resourceMap.extractFolderEntries();
        loaded = true;
    }


    @Override
    public void save() {
        super.save();
        getResourceMap().save();
        researchObject.getManifest().saveFolderClass(this);
    }


    /**
     * Create a folder instance out of an RDF/XML description.
     * 
     * @param builder
     *            builder for creating objects
     * @param researchObject
     *            research object used as RDF base
     * @param folderUri
     *            folder URI
     * @param content
     *            RDF/XML folder description
     * @return a folder instance
     * @throws BadRequestException
     *             the folder description is incorrect
     */
    public static Folder assemble(Builder builder, ResearchObject researchObject, URI folderUri, InputStream content)
            throws BadRequestException {
        Folder folder = builder.buildFolder(researchObject, folderUri, builder.getUser().getUri(), DateTime.now());
        folder.resourceMap = FolderResourceMap
                .create(builder, folder, FolderResourceMap.generateResourceMapUri(folder));
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read(content, researchObject.getUri().toString());
        List<Individual> folders = model.listIndividuals(RO.Folder).toList();
        Map<URI, FolderEntry> entries = new HashMap<>();
        if (folders.size() == 1) {
            Individual folderInd = folders.get(0);
            List<RDFNode> aggregatedResources = folderInd.listPropertyValues(ORE.aggregates).toList();
            for (RDFNode aggregatedResource : aggregatedResources) {
                if (aggregatedResource.isURIResource()) {
                    try {
                        URI resUri = new URI(aggregatedResource.asResource().getURI());
                        if (!researchObject.getAggregatedResources().containsKey(resUri)) {
                            throw new BadRequestException("Resource " + resUri
                                    + " is not aggregated by the research object");
                        }
                        URI entryUri = folder.getUri().resolve("entries/" + UUID.randomUUID());
                        FolderEntry entry = builder.buildFolderEntry(entryUri, researchObject.getAggregatedResources()
                                .get(resUri), folder, null);
                        entries.put(resUri, entry);
                    } catch (URISyntaxException e) {
                        throw new BadRequestException("Aggregated resource has an incorrect URI", e);
                    }
                } else {
                    throw new BadRequestException("Aggregated resources cannot be blank nodes.");
                }
            }
        } else {
            throw new BadRequestException("The entity body must define exactly one ro:Folder.");
        }
        List<Individual> folderEntries = model.listIndividuals(RO.FolderEntry).toList();
        for (Individual folderEntryInd : folderEntries) {
            URI proxyFor;
            if (folderEntryInd.hasProperty(ORE.proxyFor)) {
                RDFNode proxyForNode = folderEntryInd.getPropertyValue(ORE.proxyFor);
                if (!proxyForNode.isURIResource()) {
                    throw new BadRequestException("Folder entry ore:proxyFor must be a URI resource.");
                }
                try {
                    proxyFor = new URI(proxyForNode.asResource().getURI());
                } catch (URISyntaxException e) {
                    throw new BadRequestException("Folder entry proxy for URI is incorrect", e);
                }
            } else {
                throw new BadRequestException("Folder entries must have the ore:proxyFor property.");
            }
            if (!entries.containsKey(proxyFor)) {
                throw new BadRequestException(
                        "Found a folder entry for a resource that is not aggregated by the folder");
            }
            FolderEntry folderEntry = entries.get(proxyFor);
            if (folderEntryInd.hasProperty(RO.entryName)) {
                RDFNode nameNode = folderEntryInd.getPropertyValue(RO.entryName);
                if (!nameNode.isLiteral()) {
                    throw new BadRequestException("Folder entry ro name must be a literal");
                }
                folderEntry.setEntryName(nameNode.asLiteral().toString());
            }
        }
        for (FolderEntry folderEntry : entries.values()) {
            if (folderEntry.getEntryName() == null) {
                if (folderEntry.getProxyFor().getUri().getPath() != null) {
                    String[] segments = folderEntry.getProxyFor().getUri().getPath().split("/");
                    folderEntry.setEntryName(segments[segments.length - 1]);
                } else {
                    folderEntry.setEntryName(folderEntry.getProxyFor().getUri().getHost());
                }
            }
        }
        folder.setFolderEntries(new HashSet<>(entries.values()));
        folder.loaded = true;
        return folder;
    }


    public static Folder create(Builder builder, ResearchObject researchObject, URI folderUri, InputStream content)
            throws BadRequestException {
        Folder folder = assemble(builder, researchObject, folderUri, content);
        folder.setProxy(Proxy.create(builder, researchObject, folder));
        folder.save();
        for (FolderEntry entry : folder.getFolderEntries()) {
            entry.save();
        }
        folder.getResourceMap().serialize();
        return folder;
    }

}
