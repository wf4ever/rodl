package pl.psnc.dl.wf4ever.model.RDF;

import java.io.InputStream;
import java.net.URI;

import org.joda.time.DateTime;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.exceptions.IncorrectModelException;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.DCTerms;

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


    /**
     * Constructor.
     */
    public Thing() {
    }


    /**
     * Constructor.
     * 
     * @param uri
     *            resource URI
     */
    public Thing(URI uri) {
        this.uri = uri;
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

}
