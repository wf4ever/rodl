package pl.psnc.dl.wf4ever.model.AO;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RO.Resource;
import pl.psnc.dl.wf4ever.vocabulary.AO;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Statement;

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
     */
    public Annotation(URI uri, OntModel model) {
        this(uri);
        annotated = new ArrayList<Resource>();
        fillUp(model);
    }


    /**
     * Filling up annotated list and body field.
     * 
     * @param model
     *            onthology model
     */
    private void fillUp(OntModel model) {
        Individual source = model.getIndividual(uri.toString());
        for (Statement statement : source.listProperties(RO.annotatesAggregatedResource).toList()) {
            try {
                annotated.add(new Resource(new URI(statement.getObject().asResource().getURI())));
            } catch (URISyntaxException e) {
                continue;
            }
        }
        try {
            body = new AnnotationBody(new URI(source.getPropertyValue(AO.body).asResource().getURI()));
        } catch (URISyntaxException e) {
            body = null;
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
