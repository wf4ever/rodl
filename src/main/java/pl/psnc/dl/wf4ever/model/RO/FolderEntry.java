package pl.psnc.dl.wf4ever.model.RO;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.ORE.Proxy;
import pl.psnc.dl.wf4ever.vocabulary.ORE;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Represents an ro:FolderEntry.
 * 
 * @author piotrekhol
 * 
 */
public class FolderEntry extends Proxy {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(FolderEntry.class);

    /** Name of the resource in the folder. */
    protected String entryName;


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
     *            folder entry URI
     */
    public FolderEntry(UserMetadata user, Dataset dataset, boolean useTransactions, URI uri) {
        super(user, dataset, useTransactions, uri);
    }


    public String getEntryName() {
        return entryName;
    }


    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }


    /**
     * Generate an ro:entryName for a resource URI. The entry name is not guaranteed to be different for different URIs.
     * 
     * @param uri
     *            resource URI
     * @return entry name based on the resource URI
     */
    public static String generateEntryName(URI uri) {
        if (uri.getPath() != null && !uri.getPath().isEmpty() && !uri.getPath().equals("/")) {
            String e = Paths.get(uri.getPath()).getFileName().toString();
            if (uri.toString().endsWith("/")) {
                return e + "/";
            } else {
                return e;
            }
        } else {
            return uri.toString();
        }
    }


    public Folder getFolder() {
        return (Folder) proxyIn;
    }


    @Override
    public void save()
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        super.save();
        getFolder().getResourceMap().saveProxy(this);
        getFolder().getResourceMap().saveFolderEntryData(this);

        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            Individual entryInd = model.createIndividual(uri.toString(), RO.FolderEntry);
            com.hp.hpl.jena.rdf.model.Resource folderInd = model.createResource(getFolder().getUri().toString());
            //FIXME this is not a correct relation
            entryInd.addProperty(ORE.isAggregatedBy, folderInd);
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }


    /**
     * Delete the folder entry and the aggregation in the folder. Doesn't delete the proxied resource.
     */
    @Override
    public void delete() {
        if (getProxyFor() != null) {
            getProxyIn().getResourceMap().deleteResource(getProxyFor());
            getProxyIn().getAggregatedResources().remove(getProxyFor().getUri());
            getProxyFor().getResearchObject().getFolderEntries().remove(uri);
            getProxyFor().getResearchObject().getFolderEntriesByResourceUri().get(getProxyFor().getUri()).remove(this);
        } else {
            LOGGER.warn("No proxy for folder entry: " + this);
        }
        super.delete();
    }


    /**
     * Create a folder entry instance out of an RDF/XML description.
     * 
     * @param builder
     *            model instance builder
     * @param folder
     *            folder used as RDF base
     * @param content
     *            RDF/XML folder description
     * @return a folder instance
     * @throws BadRequestException
     *             the folder description is incorrect
     */
    public static FolderEntry assemble(Builder builder, Folder folder, InputStream content)
            throws BadRequestException {
        URI entryUri = folder.getUri().resolve("entries/" + UUID.randomUUID());
        FolderEntry entry = builder.buildFolderEntry(entryUri, null, folder, null);
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read(content, folder.getUri().toString());
        ExtendedIterator<Individual> it = model.listIndividuals(RO.FolderEntry);
        if (it.hasNext()) {
            Individual entryI = it.next();
            NodeIterator it2 = entryI.listPropertyValues(ORE.proxyFor);
            if (it2.hasNext()) {
                RDFNode proxyForResource = it2.next();
                if (proxyForResource.isURIResource()) {
                    try {
                        URI resUri = new URI(proxyForResource.asResource().getURI());
                        AggregatedResource res = folder.getResearchObject().getAggregatedResources().get(resUri);
                        entry.setProxyFor(res);
                    } catch (URISyntaxException e) {
                        throw new BadRequestException("Wrong ore:proxyFor URI", e);
                    }
                } else {
                    throw new BadRequestException("The ore:proxyFor object is not an URI resource.");
                }
            } else {
                throw new BadRequestException("ore:proxyFor is missing.");
            }
            RDFNode entryName = entryI.getPropertyValue(RO.entryName);
            if (entryName == null) {
                entry.setEntryName(FolderEntry.generateEntryName(entry.getProxyFor().getUri()));
            } else {
                entry.setEntryName(entryName.asLiteral().getString());
            }
        } else {
            throw new BadRequestException("The entity body does not define any ro:FolderEntry.");
        }
        return entry;
    }


    /**
     * Update the name of the resource in the folder.
     * 
     * @param newEntry
     *            the new entry description
     * @return this entry, updated
     */
    public FolderEntry update(FolderEntry newEntry) {
        setEntryName(newEntry.getEntryName());
        save();
        return this;
    }


    /**
     * Copy the folder entry, find the aggregated resource in the new folder's research object.
     * 
     * @param builder
     *            model instance builder
     * @param folder
     *            folder for which the new entry will be created
     * @return the new entry
     */
    public FolderEntry copy(Builder builder, Folder folder) {
        URI entryUri = folder.getUri().resolve("entries/" + UUID.randomUUID());
        URI resourceUri = folder.getResearchObject().getUri().resolve(getProxyFor().getRawPath());
        FolderEntry entry = builder.buildFolderEntry(entryUri,
            folder.getResearchObject().getAggregatedResources().get(resourceUri), folder, getEntryName());
        folder.addFolderEntry(entry);
        return entry;
    }

}
