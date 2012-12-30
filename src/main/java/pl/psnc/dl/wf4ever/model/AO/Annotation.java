package pl.psnc.dl.wf4ever.model.AO;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.exceptions.IncorrectModelException;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
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

    /** Set of annotated objects. */
    private Set<URI> annotated;

    /** The annotation body. */
    private URI body;

    /** has the annotation body been loaded. */
    private boolean loaded;


    /**
     * Constructor.
     * 
     * @param researchObject
     * 
     * @param uri
     *            Resource uri
     * @param model
     *            Ontology model
     * @throws IncorrectModelException
     *             the model contains an incorrect annotation description
     */
    public Annotation(ResearchObject researchObject, URI uri, OntModel model)
            throws IncorrectModelException {
        super(uri, researchObject);
        this.annotated = new HashSet<>();
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
    public Annotation(ResearchObject researchObject, URI uri, Set<URI> annotated, URI body) {
        super(uri, researchObject);
        this.annotated = annotated;
        this.body = body;
    }


    /**
     * Constructor.
     * 
     * @param researchObject
     *            RO aggregating the annotation
     * @param uri
     *            annotation URI
     * @param body
     *            annotation body, may be aggregated or not, may be a ro:Resource (rarely) or not
     * @param targets
     *            annotated resources, must be RO/aggregated resources/proxies
     * @param creator
     *            annotation author
     * @param created
     *            annotation creation time
     */
    public Annotation(ResearchObject researchObject, URI uri, URI proxyUri, URI body, Set<URI> targets, URI creator,
            DateTime created) {
        super(researchObject, uri, proxyUri, creator, created);
        this.body = body;
        this.annotated = targets;
        this.loaded = false;
    }


    /**
     * Constructor.
     * 
     * @param researchObject
     *            RO aggregating the annotation
     * @param uri
     *            annotation URI
     * @param body
     *            annotation body, may be aggregated or not, may be a ro:Resource (rarely) or not
     * @param target
     *            annotated resource, must be the RO/aggregated resource/proxy
     * @param creator
     *            annotation author
     * @param created
     *            annotation creation time
     */
    public Annotation(ResearchObject researchObject, URI uri, URI proxyUri, URI body, URI target, URI creator,
            DateTime created) {
        this(researchObject, uri, proxyUri, body, new HashSet<URI>(Arrays.asList(new URI[] { target })), creator,
                created);
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


    public Set<URI> getAnnotated() {
        return annotated;
    }


    public URI getBody() {
        return body;
    }


    public void setAnnotated(Set<URI> annotated) {
        this.annotated = annotated;
    }


    public void setBody(URI body) {
        this.body = body;
    }

}
