package pl.psnc.dl.wf4ever.model;

import org.junit.Before;

import pl.psnc.dl.wf4ever.common.Builder;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * Base test class for model package.
 * 
 */
public class BaseTest {

    /** Temporary dataset. */
    protected Dataset dataset;
    protected Boolean useTransactions;
    protected static UserMetadata userProfile;
    protected Builder builder;


    /**
     * Constructor.
     */
    public BaseTest() {
        dataset = TDBFactory.createDataset();
        useTransactions = false;
        userProfile = new UserMetadata("jank", "Jan Kowalski", Role.AUTHENTICATED);
        builder = new Builder(userProfile, dataset, false);
    }


    @Before
    public void setUp()
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
    }
    //try to throw it out!
    /*
    protected boolean addNamedGraph(URI graphURI, InputStream inputStream, RDFFormat rdfFormat) {
        boolean created = !containsNamedGraph(graphURI);
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            Model namedGraphModel = createOntModelForNamedGraph(graphURI);
            namedGraphModel.removeAll();
            namedGraphModel.read(inputStream, graphURI.resolve(".").toString(), rdfFormat.getName().toUpperCase());
            commitTransaction(transactionStarted);
            return created;
        } finally {
            endTransaction(transactionStarted);
        }
    }


    private OntModel createOntModelForNamedGraph(URI namedGraphURI) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
            dataset.getNamedModel(namedGraphURI.toString()));
        return ontModel;
    }


    protected boolean beginTransaction(ReadWrite write) {
        if (useTransactions && dataset.supportsTransactions() && !dataset.isInTransaction()) {
            dataset.begin(write);
            return true;
        } else {
            return false;
        }
    }


    protected void commitTransaction(boolean wasStarted) {
        if (useTransactions && dataset.supportsTransactions() && wasStarted) {
            dataset.commit();
        }
    }


    protected void endTransaction(boolean wasStarted) {
        if (useTransactions && dataset.supportsTransactions() && wasStarted) {
            dataset.end();
        }
    }


    public boolean containsNamedGraph(URI graphURI) {
        boolean transactionStarted = beginTransaction(ReadWrite.READ);
        try {
            return dataset.containsNamedModel(SafeURI.URItoString(graphURI));
        } finally {
            endTransaction(transactionStarted);
        }
    }
    */
}