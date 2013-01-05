package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import pl.psnc.dl.wf4ever.common.Builder;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.ORE.ResourceMap;
import pl.psnc.dl.wf4ever.vocabulary.ORE;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

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
            Individual folderInd = model.createIndividual(getFolder().getUri().toString(), RO.Folder);
            Resource proxyForR = model.getResource(entry.getProxyFor().getUri().toString());
            proxyForR.addProperty(ORE.isAggregatedBy, folderInd);
            folderInd.addProperty(ORE.aggregates, proxyForR);
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


    /**
     * Identify ro:FolderEntries, aggregated by the folder.
     * 
     * @return a set of resources (not loaded)
     */
    public Set<FolderEntry> extractFolderEntries() {
        boolean transactionStarted = beginTransaction(ReadWrite.READ);
        try {
            Set<FolderEntry> entries = new HashSet<>();
            String queryString = String
                    .format(
                        "PREFIX ore: <%s> PREFIX ro: <%s> SELECT ?entry ?resource ?name WHERE { ?entry a ro:FolderEntry ; ro:entryName ?name ; ore:proxyFor ?resource ; ore:proxyIn <%s> . }",
                        ORE.NAMESPACE, RO.NAMESPACE, aggregation.getUri().toString());

            Query query = QueryFactory.create(queryString);
            QueryExecution qe = QueryExecutionFactory.create(query, model);
            try {
                ResultSet results = qe.execSelect();
                while (results.hasNext()) {
                    QuerySolution solution = results.next();
                    RDFNode r = solution.get("resource");
                    URI rUri = URI.create(r.asResource().getURI());
                    AggregatedResource proxyFor = getFolder().getResearchObject().getAggregatedResources().get(rUri);
                    RDFNode e = solution.get("entry");
                    URI eUri = URI.create(e.asResource().getURI());
                    RDFNode nameNode = solution.get("name");
                    String name = nameNode.asLiteral().getString();
                    FolderEntry entry = builder.buildFolderEntry(eUri, proxyFor, getFolder(), name);
                    entries.add(entry);
                }
            } finally {
                qe.close();
            }
            return entries;
        } finally {
            endTransaction(transactionStarted);
        }
    }


    public Folder getFolder() {
        return (Folder) aggregation;
    }


    public static FolderResourceMap create(Builder builder, Folder folder, URI resourceMapUri)
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        FolderResourceMap map = builder.buildFolderResourceMap(resourceMapUri, folder);
        map.save();
        return map;
    }


    @Override
    public void save() {
        super.save();
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            Resource manifestRes = model.createResource(getFolder().getResearchObject().getManifest().getUri()
                    .toString());
            Individual roInd = model.createIndividual(getFolder().getResearchObject().getUri().toString(),
                RO.ResearchObject);
            model.add(roInd, ORE.isDescribedBy, manifestRes);

            Resource folderRMRes = model.createResource(uri.toString());
            Individual folderInd = model.createIndividual(getFolder().getUri().toString(), RO.Folder);
            folderInd.addRDFType(ORE.Aggregation);
            model.add(folderInd, ORE.isAggregatedBy, roInd);
            model.add(folderInd, ORE.isDescribedBy, folderRMRes);
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }
}
