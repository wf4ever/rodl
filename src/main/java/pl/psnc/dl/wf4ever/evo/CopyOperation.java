package pl.psnc.dl.wf4ever.evo;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.dl.RodlException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.hibernate.HibernateUtil;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.RO.Folder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;

/**
 * Copy one research object to another.
 * 
 * @author piotrekhol
 * 
 */
public class CopyOperation implements Operation {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(CopyOperation.class);

    /** user calling this operation. */
    private UserMetadata user;

    /** resource builder. */
    private Builder builder;


    /**
     * Constructor.
     * 
     * @param builder
     *            model instance builder
     */
    public CopyOperation(Builder builder) {
        this.builder = builder;
    }


    @Override
    public void execute(JobStatus status)
            throws OperationFailedException {
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            ResearchObject targetRO;
            try {
                targetRO = ResearchObject.create(builder, status.getTarget());
            } catch (RodlException e) {
                throw new OperationFailedException("Failed to create target RO", e);
            }
            ResearchObject sourceRO = ResearchObject.get(builder, status.getCopyfrom());
            if (sourceRO == null) {
                throw new OperationFailedException("source Research Object does not exist");
            }
            Map<URI, URI> changedURIs = new HashMap<>();
            changedURIs.put(sourceRO.getManifestUri(), targetRO.getManifestUri());
            // copy the ro:Resources
            for (pl.psnc.dl.wf4ever.model.RO.Resource resource : sourceRO.getResources().values()) {
                try {
                    pl.psnc.dl.wf4ever.model.RO.Resource resource2 = targetRO.aggregateCopy(resource);
                    changedURIs.put(resource.getUri(), resource2.getUri());
                } catch (BadRequestException e) {
                    LOGGER.warn("Failed to copy the resource", e);
                }
            }
            //copy the annotations
            for (Annotation annotation : sourceRO.getAnnotations().values()) {
                try {
                    Annotation annotation2 = targetRO.annotateCopy(annotation);
                    changedURIs.put(annotation.getUri(), annotation2.getUri());
                } catch (BadRequestException e) {
                    LOGGER.warn("Failed to copy the annotation", e);
                }
            }
            //copy the folders
            for (Folder folder : sourceRO.getFolders().values()) {
                Folder folder2 = targetRO.copy(folder);
                changedURIs.put(folder.getUri(), folder2.getUri());
            }
            //            OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
            //            model.read(sourceRO.getManifestUri().toString());
            //            Individual source = model.getIndividual(sourceRO.getUri().toString());
            //            if (source == null) {
            //                throw new OperationFailedException("The manifest does not describe the research object");
            //            }
            //
            //            List<RDFNode> aggregatedResources = source.listPropertyValues(ORE.aggregates).toList();
            //            for (RDFNode aggregatedResource : aggregatedResources) {
            //                if (Thread.interrupted()) {
            //                    try {
            //                        targetRO.delete();
            //                    } catch (RodlException e) {
            //                        LOGGER.error("Could not delete the target when aborting: " + status.getTarget(), e);
            //                    }
            //                    return;
            //                }
            //                if (!aggregatedResource.isURIResource()) {
            //                    LOGGER.warn("Aggregated node " + aggregatedResource.toString() + " is not a URI resource");
            //                    continue;
            //                }
            //                Individual resource = aggregatedResource.as(Individual.class);
            //                URI resourceURI = URI.create(resource.getURI());
            //                if (resource.hasRDFType(RO.AggregatedAnnotation)) {
            //                    Resource annBody = resource.getPropertyResourceValue(AO.body);
            //                    Set<Thing> targets = new HashSet<>();
            //                    List<RDFNode> annotationTargets = resource.listPropertyValues(RO.annotatesAggregatedResource)
            //                            .toList();
            //                    for (RDFNode annTarget : annotationTargets) {
            //                        if (!annTarget.isURIResource()) {
            //                            LOGGER.warn("Annotation target " + annTarget.toString() + " is not a URI resource");
            //                            continue;
            //                        }
            //                        targets.add(new Thing(user, URI.create(annTarget.asResource().getURI())));
            //                    }
            //                    try {
            //                        //FIXME use a dedicated class for an Annotation
            //                        String[] segments = resource.getURI().split("/");
            //                        targetRO.annotate(URI.create(annBody.getURI()), targets, segments[segments.length - 1]);
            //                    } catch (RodlException | BadRequestException e1) {
            //                        LOGGER.error("Could not add the annotation", e1);
            //                    }
            //                } else if (resource.hasRDFType(RO.Folder)) {
            //                    //                    Folder folder = ROSRService.SMS.get().getFolder(resourceURI);
            //                    //                    folder.setUri(targetRO.getUri().resolve(sourceRO.getUri().relativize(folder.getUri())));
            //                    //                    folder.setProxy(null);
            //                    //                    for (FolderEntry entry : folder.getFolderEntries().values()) {
            //                    //                        entry.setUri(null);
            //                    //                        entry.setProxyIn(folder);
            //                    //                        entry.setProxyFor(targetRO.getUri().resolve(
            //                    //                            sourceRO.getUri().relativize(entry.getProxyFor().getUri())));
            //                    //                    }
            //                    //                    try {
            //                    //                        ROSRService.createFolder(targetRO, folder);
            //                    //                    } catch (DigitalLibraryException | NotFoundException | AccessDeniedException e1) {
            //                    //                        throw new OperationFailedException("Could not create copy folder: " + resourceURI, e1);
            //                    //                    }
            //                } else {
            //                    if (isInternalResource(resourceURI, status.getCopyfrom())) {
            //                        try {
            //                            Client client = Client.create();
            //                            WebResource webResource = client.resource(resourceURI.toString());
            //                            ClientResponse response = webResource.get(ClientResponse.class);
            //                            URI resourcePath = status.getCopyfrom().relativize(resourceURI);
            //                            try {
            //                                pl.psnc.dl.wf4ever.model.RO.Resource r = targetRO.aggregate(resourcePath.toString(),
            //                                    response.getEntityInputStream(), response.getType().toString());
            //                                changedURIs.put(resourceURI, r.getUri());
            //                            } catch (RodlException e) {
            //                                LOGGER.warn("Failed to aggregate the resource", e);
            //                            }
            //                        } catch (RodlException | BadRequestException e) {
            //                            throw new OperationFailedException("Could not create aggregate internal resource: "
            //                                    + resourceURI, e);
            //                        }
            //                    } else {
            //                        try {
            //                            targetRO.aggregate(resourceURI);
            //                        } catch (RodlException e) {
            //                            throw new OperationFailedException("Could not create aggregate external resource: "
            //                                    + resourceURI, e);
            //                        }
            //                    }
            //                }
            //            }
            for (Map.Entry<URI, URI> e : changedURIs.entrySet()) {
                ROSRService.SMS.get().changeURIInManifestAndAnnotationBodies(targetRO, e.getKey(), e.getValue(), false);
            }
            for (Map.Entry<URI, URI> e : changedURIs.entrySet()) {
                ROSRService.SMS.get().changeURIInManifestAndAnnotationBodies(targetRO, e.getKey(), e.getValue(), true);
            }
            //TODO!!
            //make me easier!
            //            Annotation a = targetRO.getAnnotationsByBodyUri().get(targetRO.getEvoInfoBody().getUri()).iterator().next();
            //            a.getBody().delete();
        } finally {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }

    }

}
