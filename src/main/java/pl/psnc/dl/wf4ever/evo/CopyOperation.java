package pl.psnc.dl.wf4ever.evo;

import java.net.URI;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.auth.SecurityFilter;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

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

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
        model.read(status.getCopyfrom().resolve(Constants.MANIFEST_PATH).toString());

        Individual source = model.getIndividual(status.getCopyfrom().toString());
        if (source == null) {
            throw new OperationFailedException("The manifest does not describe the research object");
        }
        OntProperty aggregates = model.getOntProperty("http://purl.org/ao/aggregates");
        NodeIterator it = source.listPropertyValues(aggregates);
        while (it.hasNext()) {
            RDFNode node = it.next();
            if (!node.isURIResource()) {
                LOG.warn("Node " + node.toString() + " is not an URI resource");
                continue;
            }
            Resource resource = node.asResource();
            URI resourceURI = URI.create(resource.getURI());
            if (isInternalResource(resourceURI, status.getCopyfrom())) {

            } else {
                //                ROSRService.
            }
        }

    }


    private boolean isInternalResource(URI resource, URI ro) {
        return resource.toString().startsWith(ro.toString());
    }
}
