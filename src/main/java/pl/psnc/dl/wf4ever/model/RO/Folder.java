package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.ORE.Aggregation;

/**
 * ro:Folder.
 * 
 * @author piotrekhol
 * 
 */
public class Folder extends Resource implements Aggregation {

    /** folder entries. */
    private List<FolderEntry> folderEntries = new ArrayList<FolderEntry>();

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
     * @param proxyURI
     *            URI of the proxy
     * @param resourceMap
     *            Resource map (graph with folder description) URI
     * @param creator
     *            author of the resource
     * @param created
     *            creation date
     * @param rootFolder
     *            is the folder a root folder in the RO
     */
    public Folder(UserMetadata user, ResearchObject researchObject, URI uri, URI proxyURI, URI resourceMap,
            URI creator, DateTime created, boolean rootFolder) {
        super(user, researchObject, uri, proxyURI, creator, created, null);
        this.resourceMap = new FolderResourceMap(user, this, getResourceMapUri(), creator, created);
        this.rootFolder = rootFolder;
        this.loaded = false;
    }


    public List<FolderEntry> getFolderEntries() {
        return folderEntries;
    }


    public void setFolderEntries(List<FolderEntry> folderEntries) {
        this.folderEntries = folderEntries;
    }


    /**
     * Return a URI of an RDF graph that describes the folder. If folder URI is null, return null. If folder URI path is
     * empty, return folder.rdf (i.e. example.com becomes example.com/folder.rdf). Otherwise use the last path segment
     * (i.e. example.com/foobar/ becomes example.com/foobar/foobar.rdf). RDF/XML file extension is used.
     * 
     * @return RDF graph URI or null if folder URI is null
     */
    public URI getResourceMapUri() {
        return getResourceMapUri(null);
    }


    public FolderResourceMap getResourceMap() {
        return resourceMap;
    }


    /**
     * Return the URI of resource map in a selected RDF format.
     * 
     * @param format
     *            RDF format
     * @return resource map URI or null if folder URI is null
     */
    public URI getResourceMapUri(RDFFormat format) {
        if (uri == null) {
            return null;
        }
        String base;
        if (uri.getPath() == null || uri.getPath().isEmpty()) {
            base = "/folder";
        } else if (uri.getPath().equals("/")) {
            base = "folder";
        } else {
            String[] segments = uri.getRawPath().split("/");
            base = segments[segments.length - 1];
        }
        if (format == null || format.equals(RDFFormat.RDFXML)) {
            return uri.resolve(base + ".rdf");
        } else {
            return uri.resolve(base + "." + format.getDefaultFileExtension() + "?original=" + base + ".rdf");
        }
    }


    public boolean isLoaded() {
        return loaded;
    }


    public boolean isRootFolder() {
        return rootFolder;
    }

}
