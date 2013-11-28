package pl.psnc.dl.wf4ever.model.AO;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.eventbus.events.ROComponentAfterCreateEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROComponentBeforeCreateEvent;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.EvoBuilder;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
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

	/** logger. */
	private static final Logger LOGGER = Logger.getLogger(Annotation.class);

	/** Set of annotated objects. */
	private Set<Thing> annotated;

	/** The annotation body URI. The body may or may not be aggregated. */
	private Thing body;

	/**
	 * Constructor.
	 * 
	 * @param user
	 *            user creating the instance
	 * @param dataset
	 *            custom dataset
	 * @param useTransactions
	 *            should transactions be used. Note that not using transactions
	 *            on a dataset which already uses transactions may make it
	 *            unreadable.
	 * @param researchObject
	 *            RO aggregating the annotation
	 * @param uri
	 *            Resource uri
	 */
	public Annotation(UserMetadata user, Dataset dataset, boolean useTransactions,
			ResearchObject researchObject, URI uri) {
		super(user, dataset, useTransactions, researchObject, uri);
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

	/**
	 * Set annotation body.
	 * 
	 * @param body
	 *            the body
	 * @throws NullPointerException
	 *             if the body is null
	 */
	public void setBody(Thing body) throws NullPointerException {
		Objects.requireNonNull(body, "Body cannot be null");
		this.body = body;
	}

	/**
	 * Create a new annotation. If the body has already been aggregated, put it
	 * to the triple store and make sure it is not an ro:Resource.
	 * 
	 * @param builder
	 *            builder for creating new instances
	 * @param researchObject
	 *            RO aggregating the annotation
	 * @param uri
	 *            annotation URI
	 * @param bodyUri
	 *            annotation body, can be external or not yet aggregated
	 * @param targets
	 *            annotated resources (RO, ro:Resources or proxies)
	 * @return the annotation
	 * @throws BadRequestException
	 *             if there is no data in storage or the file format is not RDF
	 */
	public static Annotation create(Builder builder, ResearchObject researchObject, URI uri,
			URI bodyUri, Set<Thing> targets) throws BadRequestException {
		Objects.requireNonNull(bodyUri, "Body URI cannot be null");
		Objects.requireNonNull(targets, "Targets cannot be null");
		if (targets.isEmpty()) {
			throw new IllegalArgumentException("The set of targets cannot be empty");
		}
		Annotation annotation = builder.buildAnnotation(uri, researchObject,
				builder.buildThing(bodyUri), targets, builder.getUser(), DateTime.now());
		annotation.postEvent(new ROComponentBeforeCreateEvent(annotation));
		annotation.setProxy(researchObject.addProxy(annotation));
		annotation.save();
		annotation.onCreated();
		annotation.postEvent(new ROComponentAfterCreateEvent(annotation));
		return annotation;
	}

	/**
	 * Create and save a new annotation based on an annotation description.
	 * 
	 * @param builder
	 *            model instances builder
	 * @param researchObject
	 *            research object aggregating the RO
	 * @param uri
	 *            annotation URI
	 * @param content
	 *            annotation description
	 * @return an annotation instance
	 * @throws BadRequestException
	 *             if the description is not valid
	 */
	public static Annotation create(Builder builder, ResearchObject researchObject, URI uri,
			InputStream content) throws BadRequestException {
		Annotation annotation = assemble(builder, researchObject, uri, content);
		annotation.postEvent(new ROComponentBeforeCreateEvent(annotation));
		annotation.setCreated(DateTime.now());
		annotation.setCreator(builder.getUser());
		annotation.setProxy(researchObject.addProxy(annotation));
		annotation.save();
		annotation.onCreated();
		annotation.postEvent(new ROComponentAfterCreateEvent(annotation));
		return annotation;
	}

	/**
	 * Create a new annotation as a copy of another one. The URI will be
	 * different but other fields will be the same.
	 * 
	 * @param builder
	 *            model instances builder
	 * @param evoBuilder
	 *            builder of evolution properties
	 * @param researchObject
	 *            research object aggregating the annotation
	 * @return the new annotation
	 * @throws BadRequestException
	 *             if there is no data in storage or the file format is not RDF
	 */
	public Annotation copy(Builder builder, EvoBuilder evoBuilder, ResearchObject researchObject)
			throws BadRequestException {
		URI annotationUri = researchObject.getUri().resolve(getRawPath());
		if (researchObject.isUriUsed(annotationUri)) {
			throw new ConflictException("Resource already exists: " + annotationUri);
		}
		URI bodyUri;
		AggregatedResource aggregatedBody = getResearchObject().getAggregatedResources().get(
				getBody().getUri());
		if (aggregatedBody != null) {
			bodyUri = researchObject.getUri().resolve(aggregatedBody.getRawPath());
		} else {
			bodyUri = getBody().getUri();
		}
		Thing body2;
		if (researchObject.getAggregatedResources().containsKey(bodyUri)) {
			body2 = researchObject.getAggregatedResources().get(bodyUri);
		} else if (aggregatedBody != null) {
			try {
				body2 = researchObject.copy(aggregatedBody, evoBuilder);
			} catch (BadRequestException e) {
				// impossible, this was an annotation body so it must be ok
				LOGGER.error("The annotation body is incorrect", e);
				body2 = builder.buildThing(bodyUri);
			}
		} else {
			body2 = builder.buildThing(bodyUri);
		}
		Set<Thing> targets = new HashSet<>();
		for (Thing target : getAnnotated()) {
			URI targetUri;
			if (getResearchObject().getAggregatedResources().containsKey(target.getUri())) {
				targetUri = researchObject.getUri().resolve(
						getResearchObject().getAggregatedResources().get(target.getUri())
								.getRawPath());
			} else {
				// FIXME is this possible?
				targetUri = target.getUri();
			}
			if (researchObject.getAggregatedResources().containsKey(targetUri)) {
				targets.add(researchObject.getAggregatedResources().get(targetUri));
			} else {
				targets.add(builder.buildThing(targetUri));
			}
		}
		Annotation annotation2 = builder.buildAnnotation(annotationUri, researchObject, body2,
				targets, getCreator(), getCreated());
		annotation2.setCopyDateTime(DateTime.now());
		annotation2.setCopyAuthor(builder.getUser());
		annotation2.setCopyOf(this);
		annotation2.setProxy(researchObject.addProxy(annotation2));
		annotation2.save();
		AggregatedResource resource = annotation2.getResearchObject().getAggregatedResources()
				.get(annotation2.getBody().getUri());
		if (resource != null && resource.isInternal()) {
			int c = resource.updateReferences(resource.getResearchObject());
			LOGGER.debug(String.format("Updated %d triples in %s", c, resource.getUri()));
		}
		annotation2.onCreated();
		return annotation2;
	}

	@Override
	protected void save() {
		super.save();
		researchObject.getManifest().saveAnnotationData(this);
	}

	@Override
	public void delete() {
		getResearchObject().getAnnotations().remove(uri);
		getResearchObject().getAnnotationsByBodyUri().get(getBody().getUri()).remove(this);
		for (Thing thing : getAnnotated()) {
			getResearchObject().getAnnotationsByTarget().get(thing.getUri()).remove(this);
		}
		AggregatedResource resource = getResearchObject().getAggregatedResources().get(
				getBody().getUri());
		// FIXME this resource may be an internal resource of another RO, in
		// which case it shouldn't be deleted.
		if (resource != null && resource.isInternal()) {
			resource.deleteGraphAndSerialize();
			// FIXME the resource is still of class AggregatedResource and does
			// not appear in RO collections.
			getResearchObject().getManifest().saveRoResourceClass(this);
		}
		super.delete();
	}

	/**
	 * Create an annotation instance based on the description.
	 * 
	 * @param builder
	 *            model instances builder
	 * @param researchObject
	 *            research object aggregating the RO
	 * @param uri
	 *            annotation URI
	 * @param content
	 *            annotation description
	 * @return an annotation instance
	 * @throws BadRequestException
	 *             if the description is not valid
	 */
	public static Annotation assemble(Builder builder, ResearchObject researchObject, URI uri,
			InputStream content) throws BadRequestException {
		Objects.requireNonNull(content, "Content input stream cannot be null");
		URI bodyUri;
		Set<Thing> targets = new HashSet<>();
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		try {
			model.read(content, researchObject.getUri().toString());
		} catch (Exception e) {
			throw new BadRequestException("The annotation description could not be parsed", e);
		}
		List<Individual> aggregatedAnnotations = model.listIndividuals(RO.AggregatedAnnotation)
				.toList();
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
				throw new BadRequestException(
						"The ro:AggregatedAnnotation does not have an ao:body property.");
			}
			List<RDFNode> targetResources = aggregatedAnnotation.listPropertyValues(
					AO.annotatesResource).toList();
			if (targetResources.isEmpty()) {
				throw new BadRequestException(
						"The ro:AggregatedAnnotation does not have an ao:annotatesResource property.");
			}
			for (RDFNode targetResource : targetResources) {
				if (targetResource.isURIResource()) {
					URI targetUri = URI.create(targetResource.asResource().getURI());
					Thing target = validateTarget(researchObject, targetUri);
					targets.add(target);
				} else {
					throw new BadRequestException("The target is not an URI resource.");
				}
			}
		} else {
			throw new BadRequestException(
					"The entity body does not define any ro:AggregatedAnnotation.");
		}

		Annotation annotation = builder.buildAnnotation(uri, researchObject,
				builder.buildThing(bodyUri), targets);
		return annotation;
	}

	/**
	 * Find the resource that is annotated or throw a
	 * {@link BadRequestException} if not found or not allowed.
	 * 
	 * @param researchObject
	 *            RO that should contain the target
	 * @param targetUri
	 *            target URI
	 * @return a resource within the RO
	 * @throws BadRequestException
	 *             when the target is not a resource, folder, RO or proxy
	 */
	public static Thing validateTarget(ResearchObject researchObject, URI targetUri)
			throws BadRequestException {
		Thing target;
		if (researchObject.getResources().containsKey(targetUri)) {
			target = researchObject.getResources().get(targetUri);
		} else if (researchObject.getFolders().containsKey(targetUri)) {
			target = researchObject.getFolders().get(targetUri);
		} else if (researchObject.getProxies().containsKey(targetUri)) {
			target = researchObject.getProxies().get(targetUri);
		} else if (researchObject.getUri().equals(targetUri)) {
			target = researchObject;
		} else {
			throw new BadRequestException(
					String.format(
							"The annotation target %s is not RO, aggregated resource nor proxy.",
							targetUri));
		}
		return target;
	}

	/**
	 * Update this annotation with body and target of the given annotation.
	 * 
	 * @param newAnnotation
	 *            the new annotation
	 * @return this annotation, updated
	 * @throws BadRequestException
	 *             the new annotation body is not a valid RDF file
	 */
	public Annotation update(Annotation newAnnotation) throws BadRequestException {
		if (!body.getUri().equals(newAnnotation.getBody().getUri())) {
			if (getResearchObject().getAggregatedResources().containsKey(body.getUri())) {
				getResearchObject().getAggregatedResources().get(body.getUri())
						.deleteGraphAndSerialize();
			}
			// FIXME will passing a thing work?
			setBody(newAnnotation.getBody());
			if (getResearchObject().getAggregatedResources().containsKey(body.getUri())) {
				getResearchObject().getAggregatedResources().get(body.getUri())
						.saveGraphAndSerialize();
			}
		}
		setAnnotated(newAnnotation.getAnnotated());
		save();
		return this;
	}

	/**
	 * Make all changes necessary after creating the annotation.
	 * 
	 * @throws BadRequestException
	 *             if there is no data in storage or the file format is not RDF
	 */
	protected void onCreated() throws BadRequestException {
		AggregatedResource resource = getResearchObject().getAggregatedResources().get(
				getBody().getUri());
		if (resource != null && resource.isInternal()) {
			resource.saveGraphAndSerialize();
			getResearchObject().getManifest().removeRoResourceClass(resource);
		}
		getResearchObject().getManifest().serialize();
		getResearchObject().getAnnotations().put(getUri(), this);
		for (Thing target : this.getAnnotated()) {
			getResearchObject().getAnnotationsByTarget().put(target.getUri(), this);
		}
		getResearchObject().getAnnotationsByBodyUri().put(this.getBody().getUri(), this);
		getResearchObject().getAggregatedResources().put(this.getUri(), this);
	}

	@Override
	public Boolean isSpecialResource() {
		if (body != null) {
			return super.isSpecialResource() || body.isSpecialResource();
		}
		return super.isSpecialResource();
	}

}
