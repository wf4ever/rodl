package pl.psnc.dl.wf4ever.model.RDF;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.IncorrectModelException;
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

    /** all contributors. */
    protected Set<URI> contributors = new HashSet<>();

    /** last modification date. */
    protected DateTime modified;

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


    public Thing(UserMetadata user, URI uri, URI creator, DateTime created) {
        this(user, uri);
        this.creator = creator;
        this.created = created;
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


    public URI getUri(RDFFormat format) {
        if (uri == null) {
            return null;
        }
        URI base = uri.resolve(".");
        String name = base.relativize(uri).getPath();
        String specific;
        if (RDFFormat.forFileName(name) != null) {
            specific = name.substring(0, name.lastIndexOf(".")) + "." + format.getDefaultFileExtension();
        } else {
            specific = name + "." + format.getDefaultFileExtension();
        }
        return UriBuilder.fromUri(base).path(specific).queryParam("original", name).build();
    }


    public void setUri(URI uri) {
        this.uri = uri;
    }


    public URI getCreator() {
        return creator;
    }


    public void setCreator(URI creator) {
        this.creator = creator;
    }


    public DateTime getCreated() {
        return created;
    }


    public void setCreated(DateTime created) {
        this.created = created;
    }


    public DateTime getModified() {
        return modified;
    }


    public void setModified(DateTime modified) {
        this.modified = modified;
    }


    public Set<URI> getContributors() {
        return contributors;
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
     * @param base
     *            the object whose URI is the base
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public void serialize(URI base)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        String filePath = base.relativize(uri).toString();
        RDFFormat format = RDFFormat.forFileName(filePath, RDFFormat.RDFXML);
        InputStream dataStream = ROSRService.SMS.get().getNamedGraphWithRelativeURIs(uri, base, format);
        ROSRService.DL.get().createOrUpdateFile(base, filePath, dataStream, format.getDefaultMIMEType());
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
        saveContributors(this);
    }


    /**
     * Save the dcterms:creator and dcterms:created in the current model.
     * 
     * @param subject
     *            the resource being described
     */
    public void saveAuthor(Thing subject) {
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            com.hp.hpl.jena.rdf.model.Resource subjectR = model.getResource(subject.getUri().toString());
            if (!subjectR.hasProperty(DCTerms.created) && subject.getCreated() != null) {
                model.add(subjectR, DCTerms.created, model.createTypedLiteral(subject.getCreated().toCalendar(null)));
            }
            if (!subjectR.hasProperty(DCTerms.creator) && subject.getCreator() != null) {
                model.add(subjectR, DCTerms.creator, model.createResource(subject.getCreator().toString()));
            }
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }


    /**
     * Save the dcterms:contributor and dcterms:modified in the current model.
     * 
     * @param subject
     *            the resource being described
     */
    public void saveContributors(Thing subject) {
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            com.hp.hpl.jena.rdf.model.Resource subjectR = model.getResource(subject.getUri().toString());
            model.removeAll(subjectR, DCTerms.modified, null);
            if (subject.getModified() != null) {
                model.add(subjectR, DCTerms.modified, model.createTypedLiteral(subject.getModified().toCalendar(null)));
            }
            for (URI contributor : subject.getContributors()) {
                model.add(subjectR, DCTerms.contributor, model.createResource(contributor.toString()));
            }
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }


    /**
     * 
     * @return true if object represents a special object, false otherwise.
     */
    public Boolean isSpecialResource() {
        if (uri.toString().matches("(.+)?manifest\\.rdf(\\/)?") || uri.toString().matches("(.+)?evo_info\\.ttl(\\/)?")) {
            return true;
        }
        return false;
    }
}
