package pl.psnc.dl.wf4ever.model.AO;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.IncorrectModelException;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
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
    private Set<Thing> annotated;

    /** The annotation body. */
    private Thing body;

    /** has the annotation body been loaded. */
    private boolean loaded;


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     * @param researchObject
     *            RO aggregating the annotation
     * @param uri
     *            Resource uri
     * @param model
     *            Ontology model
     * 
     */
    public Annotation(UserMetadata user, ResearchObject researchObject, URI uri, OntModel model) {
        super(user, researchObject, uri);
        this.annotated = new HashSet<>();
        fillUp(model);
    }


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     * @param researchObject
     *            RO aggregating the annotation
     * @param uri
     *            Resource uri
     * @param annotated
     *            List of annotated
     * @param body
     *            Annotation body
     */
    public Annotation(UserMetadata user, ResearchObject researchObject, URI uri, Set<Thing> annotated, Thing body) {
        super(user, researchObject, uri);
        this.annotated = annotated;
        this.body = body;
    }


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
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
    public Annotation(UserMetadata user, ResearchObject researchObject, URI uri, URI proxyUri, Thing body,
            Set<Thing> targets, URI creator, DateTime created) {
        super(user, researchObject, uri, proxyUri, creator, created);
        this.body = body;
        this.annotated = targets;
        this.loaded = false;
    }


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
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
    public Annotation(UserMetadata user, ResearchObject researchObject, URI uri, URI proxyUri, Thing body,
            Thing target, URI creator, DateTime created) {
        this(user, researchObject, uri, proxyUri, body, new HashSet<Thing>(Arrays.asList(new Thing[] { target })),
                creator, created);
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
            annotated.add(new Thing(user, URI.create(resource.asResource().getURI())));
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
        body = new Thing(user, URI.create(bodyNode.asResource().getURI()));
    }


    public Set<Thing> getAnnotated() {
        return annotated;
    }


    public Thing getBody() {
        return body;
    }


    public void setAnnotated(Set<Thing> annotated) {
        this.annotated = annotated;
    }


    public void setBody(Thing body) {
        this.body = body;
    }

}
