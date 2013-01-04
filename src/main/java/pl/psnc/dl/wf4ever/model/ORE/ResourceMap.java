package pl.psnc.dl.wf4ever.model.ORE;

import java.net.URI;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.IncorrectModelException;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.vocabulary.ORE;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;

/**
 * ore:ResourceMap, including ro:Manifest.
 * 
 * @author piotrekhol
 * 
 */
public class ResourceMap extends Thing {

    protected Aggregation aggregation;


    public ResourceMap(UserMetadata user, URI uri) {
        super(user, uri);
        setNamedGraph(true);
    }


    public ResourceMap(UserMetadata user, Aggregation aggregation, URI uri) {
        super(user, uri);
        this.aggregation = aggregation;
        setNamedGraph(true);
    }


    public ResourceMap(UserMetadata user, Dataset dataset, boolean useTransactions, Aggregation aggregation, URI uri) {
        super(user, dataset, useTransactions, uri);
        this.aggregation = aggregation;
        setNamedGraph(true);
    }


    public void saveAggregation(AggregatedResource resource) {
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            Individual ro = model.getIndividual(aggregation.getUri().toString());
            if (ro == null) {
                throw new IncorrectModelException("Aggregation not found: " + aggregation.getUri());
            }
            Individual resourceR = model.createIndividual(resource.getUri().toString(), ORE.AggregatedResource);
            model.add(ro, ORE.aggregates, resourceR);
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }


    public void saveProxy(Proxy proxy) {
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            com.hp.hpl.jena.rdf.model.Resource aggregationR = model.createResource(aggregation.getUri().toString());
            com.hp.hpl.jena.rdf.model.Resource resourceR = model.createResource(proxy.getProxyFor().getUri()
                    .normalize().toString());
            Individual proxyR = model.createIndividual(proxy.getUri().toString(), ORE.Proxy);
            model.add(proxyR, ORE.proxyIn, aggregationR);
            model.add(proxyR, ORE.proxyFor, resourceR);
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }


    /**
     * Store to disk.
     * 
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public void serialize()
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        serialize(aggregation.getUri());
    }

}
