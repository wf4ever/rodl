package pl.psnc.dl.wf4ever.model.AO;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import pl.psnc.dl.wf4ever.exceptions.IncorrectModelException;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.vocabulary.AO;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Simple ao:annotation model.
 * 
 * @author pejot
 */
public class Annotation extends AggregatedResource {

    /** List of annotated objects. */
    private List<URI> annotated;

    /** The annotation body. */
    private URI body;


    /**
     * Constructor.
     * 
     * @param uri
     *            Resource uri
     */
    public Annotation(URI uri) {
        super(uri);
        annotated = new ArrayList<>();
        body = null;
    }


    /**
     * Constructor.
     * 
     * @param uri
     *            Resource uri
     * @param model
     *            Ontology model
     * @throws IncorrectModelException
     *             the model contains an incorrect annotation description
     */
    public Annotation(URI uri, OntModel model)
            throws IncorrectModelException {
        this(uri);
        fillUp(model);
    }


    /**
     * Constructor.
     * 
     * @param uri
     *            Resource uri
     * @param annotated
     *            List of annotated
     * @param body
     *            Annotation body
     */
    public Annotation(URI uri, List<URI> annotated, URI body) {
        this(uri);
        this.annotated = annotated;
        this.body = body;
    }


    /**
     * Filling up annotated list and body field.
     * 
     * @param model
     *            ontology model
     * @throws IncorrectModelException
     *             the model contains an incorrect annotation description
     */
    private void fillUp(OntModel model)
            throws IncorrectModelException {
        Individual source = model.getIndividual(uri.toString());
        for (RDFNode resource : source.listPropertyValues(RO.annotatesAggregatedResource).toList()) {
            if (resource.isLiteral()) {
                throw new IncorrectModelException(String.format("Annotation %s annotates a literal %s", uri.toString(),
                    resource.toString()));
            }
            if (resource.isAnon()) {
                throw new IncorrectModelException(String.format("Annotation %s annotates a blank node %s",
                    uri.toString(), resource.toString()));
            }
            try {
                annotated.add(new URI(resource.asResource().getURI()));
            } catch (URISyntaxException e) {
                throw new IncorrectModelException(String.format(
                    "Annotation %s annotates a resource with an invalid URI %s", uri.toString(), resource.toString()));
            }
        }
        RDFNode bodyNode = source.getPropertyValue(AO.body);
        if (bodyNode == null) {
            throw new IncorrectModelException(String.format("Annotation %s has no body", uri.toString()));
        }
        if (bodyNode.isLiteral()) {
            throw new IncorrectModelException(String.format("Annotation %s uses a literal %s as body", uri.toString(),
                bodyNode.toString()));
        }
        if (bodyNode.isAnon()) {
            throw new IncorrectModelException(String.format("Annotation %s uses a blank node %s as body",
                uri.toString(), bodyNode.toString()));
        }
        try {
            body = new URI(bodyNode.asResource().getURI());
        } catch (URISyntaxException e) {
            throw new IncorrectModelException(String.format(
                "Annotation %s uses as body a resource with an invalid URI %s", uri.toString(), bodyNode.toString()));
        }
    }


    public List<URI> getAnnotated() {
        return annotated;
    }


    public URI getBody() {
        return body;
    }


    public void setAnnotated(List<URI> annotated) {
        this.annotated = annotated;
    }


    public void setBody(URI body) {
        this.body = body;
    }

}
