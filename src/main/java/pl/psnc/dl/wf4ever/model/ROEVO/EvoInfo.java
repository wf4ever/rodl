package pl.psnc.dl.wf4ever.model.ROEVO;

import java.net.URI;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.vocabulary.RO;
import pl.psnc.dl.wf4ever.vocabulary.ROEVO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;

public class EvoInfo extends AggregatedResource {

    public EvoInfo(UserMetadata user, Dataset dataset, boolean useTransactions, ResearchObject researchObject, URI uri) {
        super(user, dataset, useTransactions, researchObject, uri);
    }


    public EvoInfo(UserMetadata user, ResearchObject researchObject, URI uri) {
        super(user, researchObject, uri);
    }


    /**
     * Create and save a new manifest.
     * 
     * @param builder
     *            model instance builder
     * @param uri
     *            manifest URI
     * @param researchObject
     *            research object that is described
     * @return a new manifest
     */
    public static EvoInfo create(Builder builder, URI uri, ResearchObject researchObject) {
        EvoInfo evoInfo = builder.buildEvoInfo(uri, researchObject, builder.getUser(), DateTime.now());
        evoInfo.save();
        return evoInfo;
    }


    @Override
    public void save()
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        super.save();
        save(getResearchObject());
    }


    private void save(ResearchObject researchObject) {
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            Individual ro = model.createIndividual(getResearchObject().getUri().toString(), RO.ResearchObject);
            ro.addRDFType(ROEVO.LiveRO);
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }

}
