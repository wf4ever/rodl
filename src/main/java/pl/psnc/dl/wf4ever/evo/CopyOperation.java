package pl.psnc.dl.wf4ever.evo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.auth.SecurityFilter;
import pl.psnc.dl.wf4ever.dlibra.ConflictException;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.rosrs.Annotation;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;
import pl.psnc.dlibra.service.AccessDeniedException;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Copy one research object to another.
 * 
 * @author piotrekhol
 * 
 */
public class CopyOperation implements Operation {

    private static final Logger LOG = Logger.getLogger(CopyOperation.class);

    private String id;


    public CopyOperation(String id) {
        this.id = id;
    }


    @Override
    public void execute(JobStatus status)
            throws OperationFailedException {
        URI target = status.getCopyfrom().resolve(id);
        int i = 1;
        while (SecurityFilter.SMS.get().containsNamedGraph(target)) {
            target = status.getCopyfrom().resolve(id + "-" + (i++));
        }
        status.setTarget(target);

        try {
            ROSRService.createResearchObject(target);
        } catch (ConflictException | DigitalLibraryException | NotFoundException e) {
            throw new OperationFailedException("Could not create the target research object", e);
        }

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
        model.read(status.getCopyfrom().resolve(Constants.MANIFEST_PATH).toString());

        Individual source = model.getIndividual(status.getCopyfrom().toString());
        if (source == null) {
            throw new OperationFailedException("The manifest does not describe the research object");
        }
        OntProperty aggregates = model.getOntProperty("http://www.openarchives.org/ore/terms/aggregates");
        NodeIterator it = source.listPropertyValues(aggregates);
        while (it.hasNext()) {
            if (Thread.interrupted()) {
                // TODO
            }
            RDFNode node = it.next();
            if (!node.isURIResource()) {
                LOG.warn("Node " + node.toString() + " is not an URI resource");
                continue;
            }
            Individual resource = node.as(Individual.class);
            URI resourceURI = URI.create(resource.getURI());
            if (resource.hasRDFType("http://purl.org/wf4ever/ro#Resource")) {
                if (isInternalResource(resourceURI, status.getCopyfrom())) {
                    try {
                        Client client = Client.create();
                        WebResource webResource = client.resource(resourceURI.toString());
                        ClientResponse response = webResource.get(ClientResponse.class);
                        ROSRService.aggregateInternalResource(target, resourceURI, response.getEntityInputStream(),
                            response.getType().toString(), null);
                    } catch (AccessDeniedException | DigitalLibraryException | NotFoundException e) {
                        throw new OperationFailedException("Could not create aggregate internal resource: "
                                + resourceURI, e);
                    }
                } else {
                    try {
                        ROSRService.aggregateExternalResource(target, resourceURI);
                    } catch (AccessDeniedException | DigitalLibraryException | NotFoundException e) {
                        throw new OperationFailedException("Could not create aggregate external resource: "
                                + resourceURI, e);
                    }
                }
            } else if (resource.hasRDFType("http://purl.org/wf4ever/ro#AggregatedAnnotation")) {
                OntProperty annotates = model.getOntProperty("http://purl.org/wf4ever/ro#annotatesAggregatedResource");
                OntProperty body = model.getOntProperty("http://purl.org/ao/body");
                Resource annBody = resource.getPropertyResourceValue(body);
                List<URI> targets = new ArrayList<>();
                NodeIterator it2 = resource.listPropertyValues(annotates);
                while (it2.hasNext()) {
                    RDFNode annTarget = it2.next();
                    if (!annTarget.isURIResource()) {
                        LOG.warn("Annotation target " + annTarget.toString() + " is not an URI resource");
                        continue;
                    }
                    targets.add(URI.create(annTarget.asResource().getURI()));
                }
                Annotation ann = new Annotation(URI.create(annBody.getURI().toString()), targets);
                ROSRService.addAnnotation(target, ann);
            }
        }

    }


    private boolean isInternalResource(URI resource, URI ro) {
        return resource.toString().startsWith(ro.toString());
    }

}
