package pl.psnc.dl.wf4ever.model.AO;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import pl.psnc.dl.wf4ever.exceptions.IncorrectModelException;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RO.Resource;
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
    private List<Resource> annotated;

    /** The annotation body. */
    private AnnotationBody body;


    /**
     * Constructor.
     * 
     * @param uri
     *            The resource uri
     */
    public Annotation(URI uri) {
        super(uri);
        annotated = new ArrayList<Resource>();
        body = null;
    }


    /**
     * Constructor.
     * 
     * @param uri
     *            The resource uri
     * @param model
     *            Ontology model
     * @throws IncorrectModelException
     *             the model contains an incorrect annotation description
     */
    public Annotation(URI uri, OntModel model)
            throws IncorrectModelException {
        this(uri);
        annotated = new ArrayList<Resource>();
        fillUp(model);
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
                annotated.add(new Resource(new URI(resource.asResource().getURI())));
            } catch (URISyntaxException e) {
                throw new IncorrectModelException(String.format(
                    "Annotation %s annotates a resource with an invalid URI %s", uri.toString(), resource.toString()));
            }
        }
        RDFNode bodyNode = source.getPropertyValue(AO.body);
        if (bodyNode.isLiteral()) {
            throw new IncorrectModelException(String.format("Annotation %s uses a literal %s as body", uri.toString(),
                bodyNode.toString()));
        }
        if (bodyNode.isAnon()) {
            throw new IncorrectModelException(String.format("Annotation %s uses a blank node %s as body",
                uri.toString(), bodyNode.toString()));
        }
        try {
            body = new AnnotationBody(new URI(bodyNode.asResource().getURI()));
        } catch (URISyntaxException e) {
            throw new IncorrectModelException(String.format(
                "Annotation %s uses as body a resource with an invalid URI %s", uri.toString(), bodyNode.toString()));
        }
    }


    public List<Resource> getAnnotated() {
        return annotated;
    }


    /**
     * Get the list of annotated resources as a list of uri.
     * 
     * @return list of uri of annotated resources
     */
    public List<URI> getAnnotatedToURIList() {
        List<URI> result = new ArrayList<URI>();
        for (Resource r : annotated) {
            result.add(r.getUri());
        }
        return result;
    }


    public AnnotationBody getBody() {
        return body;
    }

}
