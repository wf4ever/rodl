package pl.psnc.dl.wf4ever.model.AO;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.common.Builder;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.exceptions.IncorrectModelException;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.ORE.Proxy;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.vocabulary.AO;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Simple ao:annotation model.
 * 
 * @author pejot
 */
public class Annotation extends AggregatedResource {

    /** Set of annotated objects. */
    private Set<Thing> annotated;

    /** The annotation body URI. The body may or may not be aggregated. */
    private URI bodyUri;


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
    public Annotation(UserMetadata user, ResearchObject researchObject, URI uri, URI body, Set<Thing> annotated) {
        super(user, researchObject, uri);
        this.annotated = annotated;
        this.bodyUri = body;
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
     * @param bodyUri
     *            annotation body, may be aggregated or not, may be a ro:Resource (rarely) or not
     * @param target
     *            annotated resource, must be the RO/aggregated resource/proxy
     * @param creator
     *            annotation author
     * @param created
     *            annotation creation time
     */
    public Annotation(UserMetadata user, ResearchObject researchObject, URI uri, URI bodyUri, Thing target) {
        this(user, researchObject, uri, bodyUri, new HashSet<Thing>(Arrays.asList(new Thing[] { target })));
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
     */
    public Annotation(UserMetadata user, Dataset dataset, boolean useTransactions, ResearchObject researchObject,
            URI uri) {
        super(user, dataset, useTransactions, researchObject, uri);
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
        bodyUri = URI.create(bodyNode.asResource().getURI());
    }


    public Set<Thing> getAnnotated() {
        return annotated;
    }


    public URI getBodyUri() {
        return bodyUri;
    }


    public void setAnnotated(Set<Thing> annotated) {
        this.annotated = annotated;
    }


    public void setBodyUri(URI bodyUri) {
        this.bodyUri = bodyUri;
    }


    /**
     * Create a new annotation. If the body has already been aggregated, put it to the triple store and make sure it is
     * not an ro:Resource.
     * 
     * @param builder
     *            builder for creating new instances
     * @param researchObject
     *            RO aggregating the annotation
     * @param uri
     *            annotation URI
     * @param body2
     *            annotation body, can be external or not yet aggregated
     * @param targets
     *            annotated resources (RO, ro:Resources or proxies)
     * @return the annotation
     */
    public static Annotation create(Builder builder, ResearchObject researchObject, URI uri, URI bodyUri,
            Set<Thing> targets) {
        Annotation annotation = builder.buildAnnotation(researchObject, uri, bodyUri, targets, builder.getUser()
                .getUri(), DateTime.now());
        annotation.setProxy(Proxy.create(builder, researchObject, annotation));
        annotation.save();
        return annotation;
    }


    public static Annotation create(Builder builder, ResearchObject researchObject, URI uri, InputStream content)
            throws BadRequestException {
        Annotation annotation = assemble(builder, researchObject, uri, content);
        annotation.setProxy(Proxy.create(builder, researchObject, annotation));
        annotation.save();
        return annotation;
    }


    public void save() {
        super.save();
        researchObject.getManifest().saveAnnotationData(this);
    }


    public static Annotation assemble(Builder builder, ResearchObject researchObject, URI uri, InputStream content)
            throws BadRequestException {
        URI bodyUri;
        Set<Thing> targets = new HashSet<>();
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read(content, researchObject.getUri().toString());
        List<Individual> aggregatedAnnotations = model.listIndividuals(RO.AggregatedAnnotation).toList();
        if (!aggregatedAnnotations.isEmpty()) {
            Individual aggregatedAnnotation = aggregatedAnnotations.get(0);
            List<RDFNode> bodyResources = aggregatedAnnotation.listPropertyValues(AO.body).toList();
            if (!bodyResources.isEmpty()) {
                RDFNode bodyResource = bodyResources.get(0);
                if (bodyResource.isURIResource()) {
                    try {
                        bodyUri = new URI(bodyResource.asResource().getURI());
                    } catch (URISyntaxException e) {
                        throw new BadRequestException("Wrong body resource URI", e);
                    }
                } else {
                    throw new BadRequestException("The body is not an URI resource.");
                }
            } else {
                throw new BadRequestException("The ro:AggregatedAnnotation does not have a ao:body property.");
            }
            List<RDFNode> targetResources = aggregatedAnnotation.listPropertyValues(AO.annotatesResource).toList();
            for (RDFNode targetResource : targetResources) {
                if (targetResource.isURIResource()) {
                    URI targetUri = URI.create(targetResource.asResource().getURI());
                    Thing target;
                    if (researchObject.getResources().containsKey(targetUri)) {
                        target = researchObject.getResources().get(targetUri);
                    } else if (researchObject.getProxies().containsKey(targetUri)) {
                        target = researchObject.getProxies().get(targetUri);
                    } else if (researchObject.getUri().equals(targetUri)) {
                        target = researchObject;
                    } else {
                        throw new BadRequestException(String.format(
                            "The annotation target %s is not RO, aggregated resource nor proxy.", targetUri));
                    }
                    targets.add(target);
                } else {
                    throw new BadRequestException("The target is not an URI resource.");
                }
            }
        } else {
            throw new BadRequestException("The entity body does not define any ro:AggregatedAnnotation.");
        }

        Annotation annotation = builder.buildAnnotation(researchObject, uri, bodyUri, targets);
        return annotation;
    }

}
