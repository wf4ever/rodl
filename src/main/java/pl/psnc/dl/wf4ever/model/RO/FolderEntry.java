package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;
import java.nio.file.Paths;

import pl.psnc.dl.wf4ever.model.ORE.Proxy;

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
     * Default constructor.
     */
    public FolderEntry() {
        super();
    }


    /**
     * Constructor.
     * 
     * @param proxyFor
     *            URI of resource aggregated in the ro:Folder
     * @param entryName
     *            name of the resource in the folder
     */
    public FolderEntry(URI proxyFor, String entryName) {
        super();
        this.proxyFor = proxyFor;
        this.entryName = entryName;
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
}
