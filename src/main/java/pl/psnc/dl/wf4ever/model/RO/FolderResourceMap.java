package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;

import pl.psnc.dl.wf4ever.common.Builder;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.ORE.ResourceMap;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Literal;

public class FolderResourceMap extends ResourceMap {

    public FolderResourceMap(UserMetadata user, Dataset dataset, boolean useTransactions, Folder folder, URI uri) {
        super(user, dataset, useTransactions, folder, uri);
    }


    public FolderResourceMap(UserMetadata user, Folder folder, URI uri) {
        super(user, folder, uri);
    }


    public void saveFolderEntryData(FolderEntry entry) {
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            Individual entryInd = model.createIndividual(entry.getUri().toString(), RO.FolderEntry);
            Literal name = model.createLiteral(entry.getEntryName());
            model.add(entryInd, RO.entryName, name);
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }

    }


    /**
     * Return a URI of an RDF graph that describes the folder. If folder URI is null, return null. If folder URI path is
     * empty, return folder.rdf (i.e. example.com becomes example.com/folder.rdf). Otherwise use the last path segment
     * (i.e. example.com/foobar/ becomes example.com/foobar/foobar.rdf). RDF/XML file extension is used.
     * 
     * @return RDF graph URI or null if folder URI is null
     */
    public static URI generateResourceMapUri(Folder folder) {
        if (folder.getUri() == null) {
            return null;
        }
        String base;
        if (folder.getUri().getPath() == null || folder.getUri().getPath().isEmpty()) {
            base = "/folder";
        } else if (folder.getUri().getPath().equals("/")) {
            base = "folder";
        } else {
            String[] segments = folder.getUri().getRawPath().split("/");
            base = segments[segments.length - 1];
        }
        return folder.getUri().resolve(base + ".rdf");
    }


    public static FolderResourceMap get(Builder builder, URI uri, Folder folder) {
        FolderResourceMap map = builder.buildFolderResourceMap(uri, folder);
        return map;
    }

}
