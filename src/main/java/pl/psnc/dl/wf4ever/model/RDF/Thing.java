package pl.psnc.dl.wf4ever.model.RDF;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.IncorrectModelException;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceTdb;
import pl.psnc.dl.wf4ever.vocabulary.AO;
import pl.psnc.dl.wf4ever.vocabulary.ORE;
import pl.psnc.dl.wf4ever.vocabulary.W4E;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.DCTerms;

import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;

/**
 * The root class for the model.
 * 
 * @author piotrekhol
 * 
 */
public class Thing {

    /** resource URI. */
    protected URI uri;

    /** creator URI. */
    protected URI creator;

    /** creation date. */
    protected DateTime created;

    /** Use tdb transactions. */
    protected boolean useTransactions;

    /** Jena dataset. */
    private Dataset dataset;

    /** Triple store location. */
    private static final String TRIPLE_STORE_DIR = getStoreDirectory("connection.properties");

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(SemanticMetadataServiceTdb.class);

    /** Jena model of the named graph. */
    protected OntModel model;

    /** User creating the instance. */
    protected UserMetadata user;

    /** Is the resource a named graph in the triplestore. True for manifest, annotation bodies and folder resource maps. */
    protected boolean namedGraph = false;

    static {
        init();
    }


    /**
     * Constructor that allows to specify a custom dataset.
     * 
     * @param user
     *            user creating the instance
     * @param dataset
     *            custom dataset
     * @param useTransactions
     *            should transactions be used. Note that not using transactions on a dataset which already uses
     *            transactions may make it unreadable.
     */
    public Thing(UserMetadata user, Dataset dataset, Boolean useTransactions) {
        this.user = user;
        this.dataset = dataset;
        this.useTransactions = useTransactions;
    }


    /**
     * Constructor that allows to specify a custom dataset that doesn't use transactions.
     * 
     * @param user
     *            user creating the instance
     * @param dataset
     *            custom dataset
     */
    public Thing(UserMetadata user, Dataset dataset) {
        this(user, dataset, false);
    }


    /**
     * Constructor that uses a default dataset on a local drive using transactions.
     * 
     * @param user
     *            user creating the instance
     */
    public Thing(UserMetadata user) {
        this(user, null, true);
    }


    /**
     * Constructor that uses a default dataset on a local drive using transactions.
     * 
     * @param user
     *            user creating the instance
     * @param uri
     *            resource URI
     */
    public Thing(UserMetadata user, URI uri) {
        this(user);
        this.uri = uri;
    }


    /**
     * Init .
     * 
     */
    public static void init() {
        TDB.getContext().set(TDB.symUnionDefaultGraph, true);
        W4E.DEFAULT_MODEL.setNsPrefixes(W4E.STANDARD_NAMESPACES);
    }


    public URI getUri() {
        return uri;
    }


    public void setUri(URI uri) {
        this.uri = uri;
    }


    public URI getCreator() {
        return creator;
    }


    public DateTime getCreated() {
        return created;
    }


    public boolean isNamedGraph() {
        return namedGraph;
    }


    public void setNamedGraph(boolean namedGraph) {
        this.namedGraph = namedGraph;
    }


    /**
     * Take out an RDF graph from the triplestore and serialize it in storage (e.g. dLibra) with relative URI
     * references.
     * 
     * @param researchObject
     *            RO URI
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public void serialize(ResearchObject researchObject)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        String filePath = researchObject.getUri().relativize(uri).toString();
        RDFFormat format = RDFFormat.forFileName(filePath, RDFFormat.RDFXML);
        InputStream dataStream = ROSRService.SMS.get().getNamedGraphWithRelativeURIs(uri, researchObject, format);
        ROSRService.DL.get().createOrUpdateFile(researchObject.getUri(), filePath, dataStream,
            format.getDefaultMIMEType());
    }


    /**
     * Return this resource as a named graph in a selected RDF format.
     * 
     * @param rdfFormat
     *            RDF format
     * @return an input stream or null of no model is found
     */
    public InputStream getGraphAsInputStream(RDFFormat rdfFormat) {
        boolean transactionStarted = beginTransaction(ReadWrite.READ);
        try {
            if (!dataset.containsNamedModel(uri.toString())) {
                return null;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (rdfFormat.supportsContexts()) {
                Dataset tmpDataset = TDBFactory.createDataset();
                addNamedModelsRecursively(tmpDataset);

                NamedGraphSet ngs = new NamedGraphSetImpl();
                Iterator<String> it = tmpDataset.listNames();
                while (it.hasNext()) {
                    String graphUri = it.next();
                    Model ng4jModel = ModelFactory.createModelForGraph(ngs.createGraph(graphUri));
                    Model tdbModel = tmpDataset.getNamedModel(graphUri);
                    List<Statement> statements = tdbModel.listStatements().toList();
                    ng4jModel.add(statements);
                }
                ngs.write(out, rdfFormat.getName().toUpperCase(), null);
            } else {
                dataset.getNamedModel(uri.toString()).write(out, rdfFormat.getName().toUpperCase());
            }
            return new ByteArrayInputStream(out.toByteArray());
        } finally {
            endTransaction(transactionStarted);
        }
    }


    /**
     * Find the dcterms:creator of the RO.
     * 
     * @param model
     *            manifest model
     * @return creator URI or null if not defined
     * @throws IncorrectModelException
     *             incorrect manifest
     */
    protected URI extractCreator(OntModel model)
            throws IncorrectModelException {
        Individual ro = model.getIndividual(uri.toString());
        if (ro == null) {
            throw new IncorrectModelException("RO not found in the manifest" + uri);
        }
        com.hp.hpl.jena.rdf.model.Resource c = ro.getPropertyResourceValue(DCTerms.creator);
        return c != null ? URI.create(c.getURI()) : null;
    }


    /**
     * Find the dcterms:created date of the RO.
     * 
     * @param model
     *            manifest model
     * @return creation date or null if not defined
     * @throws IncorrectModelException
     *             incorrect manifest
     */
    protected DateTime extractCreated(OntModel model)
            throws IncorrectModelException {
        Individual ro = model.getIndividual(uri.toString());
        if (ro == null) {
            throw new IncorrectModelException("RO not found in the manifest" + uri);
        }
        RDFNode d = ro.getPropertyValue(DCTerms.created);
        if (d == null || !d.isLiteral()) {
            return null;
        }
        return DateTime.parse(d.asLiteral().getString());
    }


    @Override
    public int hashCode() {
        return uri.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Thing)) {
            return false;
        }
        Thing that = (Thing) obj;
        return that.uri.equals(this.uri);
    }


    @Override
    public String toString() {
        return getUri().toString();
    }


    /**
     * Start a TDB transaction provided that the flag useTransactions is set, the dataset supports transactions and
     * there is no open transaction. According to TDB, many read or one write transactions are allowed.
     * 
     * @param write
     *            read or write
     * @return true if a new transaction has been started, false otherwise
     */
    protected boolean beginTransaction(ReadWrite write) {
        if (dataset == null) {
            dataset = TDBFactory.createDataset(TRIPLE_STORE_DIR);
        }
        if (useTransactions && dataset.supportsTransactions() && !dataset.isInTransaction()) {
            dataset.begin(write);
            if (write == ReadWrite.READ) {
                model = getOntModel();
            } else {
                model = createOntModel();
            }
            return true;
        } else {
            return false;
        }
    }


    /**
     * Commit the transaction provided that the flag useTransactions is set, the dataset supports transactions and the
     * parameter is true.
     * 
     * @param wasStarted
     *            a convenience parameter to specify if the transaction should be committed
     */
    protected void commitTransaction(boolean wasStarted) {
        if (useTransactions && dataset.supportsTransactions() && wasStarted) {
            dataset.commit();
        }
    }


    /**
     * End the transaction provided that the flag useTransactions is set, the dataset supports transactions and the
     * parameter is true.
     * 
     * @param wasStarted
     *            a convenience parameter to specify if the transaction should be ended
     */
    protected void endTransaction(boolean wasStarted) {
        if (useTransactions && dataset.supportsTransactions() && wasStarted) {
            if (model != null) {
                TDB.sync(model);
                model = null;
            }
            dataset.end();
        }
    }


    /**
     * Abort the transaction provided that the flag useTransactions is set, the dataset supports transactions and the
     * parameter is true.
     * 
     * @param wasStarted
     *            a convenience parameter to specify if the transaction should be aborted
     */
    protected void abortTransaction(boolean wasStarted) {
        if (useTransactions && dataset.supportsTransactions() && wasStarted) {
            dataset.abort();
        }
    }


    // *****Private***** 

    /**
     * Load the triple store location from the properties file. In case of any exceptions, log them and return null.
     * 
     * @param filename
     *            properties file name
     * @return the path to the triple store directory
     */
    private static String getStoreDirectory(String filename) {
        try (InputStream is = Thing.class.getClassLoader().getResourceAsStream(filename)) {
            Properties props = new Properties();
            props.load(is);
            return props.getProperty("store.directory");

        } catch (Exception e) {
            LOGGER.error("Trple store location can not be loaded from the properties file", e);
        }
        return null;
    }


    /**
     * Add this model to a dataset and call this method recursively for all dependent models, unless they have already
     * been added to the dataset.
     * 
     * @param tmpDataset
     *            the dataset to which to add the model
     */
    private void addNamedModelsRecursively(Dataset tmpDataset) {
        boolean transactionStarted = beginTransaction(ReadWrite.READ);
        try {
            if (model == null) {
                LOGGER.warn("Could not find model for URI " + uri);
                return;
            }
            tmpDataset.addNamedModel(uri.toString(), model);
            List<RDFNode> it = model.listObjectsOfProperty(AO.body).toList();
            it.addAll(model.listObjectsOfProperty(ORE.isDescribedBy).toList());
            for (RDFNode namedModelRef : it) {
                URI childURI = URI.create(namedModelRef.asResource().getURI());
                if (dataset.containsNamedModel(childURI.toString())
                        && !tmpDataset.containsNamedModel(childURI.toString())) {
                    Thing relatedModel = new Thing(user, childURI);
                    relatedModel.addNamedModelsRecursively(tmpDataset);
                }
            }
        } finally {
            endTransaction(transactionStarted);
        }
    }


    /**
     * Create a new model for this resource or open an existing one.
     * 
     * @return an ontology model
     */
    public OntModel createOntModel() {
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
                dataset.getNamedModel(uri.toString()));
            commitTransaction(transactionStarted);
            return ontModel;
        } finally {
            endTransaction(transactionStarted);
        }
    }


    /**
     * Get the model for this resource or null if doesn't exist.
     * 
     * @return an ontology model or null
     */
    public OntModel getOntModel() {
        boolean transactionStarted = beginTransaction(ReadWrite.READ);
        try {
            if (!dataset.containsNamedModel(uri.toString())) {
                return null;
            }
            OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
                dataset.getNamedModel(uri.toString()));
            return ontModel;
        } finally {
            endTransaction(transactionStarted);
        }
    }


    /**
     * Save the resource to the triplestore and data storage backend.
     * 
     * @throws ConflictException
     * @throws DigitalLibraryException
     * @throws AccessDeniedException
     * @throws NotFoundException
     */
    public void save()
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        //        this.model = createOntModel();
    }


    /**
     * Save the current user and the current time as dcterms:creator and dcterms:created.
     * 
     * @param subject
     *            the resource being described
     */
    public void saveAuthor(Thing subject) {
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            com.hp.hpl.jena.rdf.model.Resource subjectR = model.getResource(subject.getUri().toString());
            if (!subjectR.hasProperty(DCTerms.created)) {
                model.add(subjectR, DCTerms.created, model.createTypedLiteral(Calendar.getInstance()));
            }
            if (!subjectR.hasProperty(DCTerms.creator) && user != null) {
                model.add(subjectR, DCTerms.creator, model.createResource(user.getUri().toString()));
            }
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }


    /**
     * Save the current user and the current time as dcterms:contributor and dcterms:modified.
     * 
     * @param subject
     *            the resource being described
     */
    public void saveContributors(Thing subject) {
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            com.hp.hpl.jena.rdf.model.Resource subjectR = model.getResource(subject.getUri().toString());
            model.removeAll(subjectR, DCTerms.modified, null);
            model.add(subjectR, DCTerms.modified, model.createTypedLiteral(Calendar.getInstance()));
            if (user != null) {
                model.add(subjectR, DCTerms.contributor, model.createResource(user.getUri().toString()));
            }
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }
}
