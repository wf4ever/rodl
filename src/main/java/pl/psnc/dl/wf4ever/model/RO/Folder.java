package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.rio.RDFFormat;

/**
 * ro:Folder.
 * 
 * @author piotrekhol
 * 
 */
public class Folder extends Resource {

    /** folder entries. */
    private List<FolderEntry> folderEntries = new ArrayList<FolderEntry>();


    public List<FolderEntry> getFolderEntries() {
        return folderEntries;
    }


    public void setFolderEntries(List<FolderEntry> folderEntries) {
        this.folderEntries = folderEntries;
    }


    /**
     * Return a URI of an RDF graph that describes the folder. If folder URI is null, return null. If folder URI path is
     * empty, return folder.ttl (i.e. example.com becomes example.com/folder.rdf). Otherwise use the last path segment
     * (i.e. example.com/foobar/ becomes example.com/foobar/foobar.rdf). RDF/XML file extension is used.
     * 
     * @return RDF graph URI or null if folder URI is null
     */
    public URI getResourceMapUri() {
        return getResourceMapUri(null);
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
        if (uri.getPath() == null) {
            base = "folder";
        } else {
            String[] segments = uri.getPath().split("/");
            base = segments[segments.length - 1];
        }
        if (format == null || format.equals(RDFFormat.RDFXML)) {
            return uri.resolve(base + ".rdf");
        } else {
            return uri.resolve(base + "." + format.getDefaultFileExtension() + "?original=" + base + ".rdf");
        }
    }
}
