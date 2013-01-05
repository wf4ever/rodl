package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;
import java.util.Set;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.common.Builder;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.ORE.Aggregation;

import com.hp.hpl.jena.query.Dataset;

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
        resourceMap = FolderResourceMap.create(builder, this, resourceMapUri);
        folderEntries = resourceMap.extractFolderEntries();
        loaded = true;
    }

}
