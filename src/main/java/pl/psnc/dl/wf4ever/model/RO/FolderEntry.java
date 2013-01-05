package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;
import java.nio.file.Paths;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.ORE.Proxy;
import pl.psnc.dl.wf4ever.vocabulary.ORE;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;

/**
 * Represents an ro:FolderEntry.
 * 
 * @author piotrekhol
 * 
 */
public class FolderEntry extends Proxy {

    /** Name of the resource in the folder. */
    protected String entryName;


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     */
    public FolderEntry(UserMetadata user, URI uri) {
        super(user, uri);
    }


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
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
            Individual folderInd = model.createIndividual(getFolder().getUri().toString(), RO.Folder);
            entryInd.addProperty(ORE.isAggregatedBy, folderInd);
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }
}
