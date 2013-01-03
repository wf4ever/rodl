package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.ORE.ResourceMap;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Literal;

public class FolderResourceMap extends ResourceMap {

    public FolderResourceMap(UserMetadata user, Folder folder, URI uri, URI creator, DateTime created) {
        super(user, folder, uri, creator, created);
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
}
