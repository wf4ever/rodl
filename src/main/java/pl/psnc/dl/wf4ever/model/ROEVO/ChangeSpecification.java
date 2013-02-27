package pl.psnc.dl.wf4ever.model.ROEVO;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.Folder;
import pl.psnc.dl.wf4ever.model.ROEVO.Change.ChangeType;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class ChangeSpecification extends Thing {

    private final ImmutableResearchObject researchObject;

    private Set<Change> changes = new HashSet<>();


    public ChangeSpecification(UserMetadata user, Dataset dataset, Boolean useTransactions, URI uri,
            ImmutableResearchObject immutableResearchObject) {
        super(user, dataset, useTransactions, uri);
        this.researchObject = immutableResearchObject;
    }


    public static ChangeSpecification create(Builder builder, ImmutableResearchObject immutableResearchObject) {
        URI uri = immutableResearchObject.getUri().resolve(".ro/change_specifications/" + UUID.randomUUID().toString());
        ChangeSpecification changeSpecification = builder.buildChangeSpecification(uri, immutableResearchObject);
        changeSpecification.save();
        return changeSpecification;
    }


    @Override
    public void save() {
        super.save();
        researchObject.getImmutableEvoInfo().saveChangeSpecification(this);
        for (Change change : changes) {
            change.save();
        }
    }


    public ImmutableResearchObject getResearchObject() {
        return researchObject;
    }


    public Set<Change> getChanges() {
        return changes;
    }


    @Override
    public void delete() {
        for (Change change : changes) {
            change.delete();
        }
        researchObject.getImmutableEvoInfo().deleteResource(this);
        super.delete();
    }


    public Set<Change> createChanges(ImmutableResearchObject previousRO) {
        //FIXME this could use the "copyOf" property instead of URI lookup
        for (AggregatedResource resource : researchObject.getAggregatedResources().values()) {
            URI previousUri = previousRO.getUri().resolve(resource.getRawPath());
            if (!resource.isSpecialResource() && !previousRO.isUriUsed(previousUri)) {
                changes.add(Change.create(builder, this, resource, ChangeType.ADDITION));
            }
        }
        for (AggregatedResource resource : previousRO.getAggregatedResources().values()) {
            URI newUri = researchObject.getUri().resolve(resource.getRawPath());
            if (!resource.isSpecialResource() && !researchObject.isUriUsed(newUri)) {
                changes.add(Change.create(builder, this, resource, ChangeType.REMOVAL));
            }
        }
        for (AggregatedResource resource : researchObject.getAggregatedResources().values()) {
            if (!resource.isSpecialResource()) {
                URI previousUri = previousRO.getUri().resolve(resource.getRawPath());
                if (previousRO.isUriUsed(previousUri)) {
                    AggregatedResource previousResource = previousRO.getAggregatedResources().get(previousUri);
                    if (previousResource == null || !areEqual(previousResource, resource)) {
                        changes.add(Change.create(builder, this, resource, ChangeType.MODIFICATION));
                    }
                }
            }
        }
        return changes;
    }


    /**
     * Compares two resources. Annotations are compared by their properties, folders by their folder entries, and
     * resource by their named graphs or serialization checksums.
     * 
     * @param previousResource
     *            one resource
     * @param resource
     *            another resource
     * @return true if they are equal, false otherwise
     */
    private boolean areEqual(AggregatedResource previousResource, AggregatedResource resource) {
        if (previousResource instanceof Folder && resource instanceof Folder) {
            return foldersAreEqual((Folder) previousResource, (Folder) resource);
        }
        if (previousResource instanceof Annotation && resource instanceof Annotation) {
            return annotationsAreEqual((Annotation) previousResource, (Annotation) resource);
        }
        return aggregatedResourcesAreEqual(previousResource, resource);
    }


    /**
     * Compare two aggregated resources. If they are both internal, compare checksums. If they are both named graphs,
     * compared relative models. Otherwise, return false.
     * 
     * @param previousResource
     *            one resource
     * @param resource
     *            another resource
     * @return true if they are equal, false otherwise
     */
    private boolean aggregatedResourcesAreEqual(AggregatedResource previousResource, AggregatedResource resource) {
        if (previousResource.isInternal() && resource.isInternal()) {
            return previousResource.getStats().getChecksum().equals(resource.getStats().getChecksum());
        } else if (previousResource.isNamedGraph() && resource.isNamedGraph()) {
            Model model1 = ModelFactory.createDefaultModel();
            Model model2 = ModelFactory.createDefaultModel();
            model1.read(previousResource.getGraphAsInputStreamWithRelativeURIs(previousResource.getResearchObject()
                    .getUri(), RDFFormat.RDFXML), "");
            model2.read(
                resource.getGraphAsInputStreamWithRelativeURIs(resource.getResearchObject().getUri(), RDFFormat.RDFXML),
                "");
            return model1.isIsomorphicWith(model2);
        } else {
            return false;
        }
    }


    /**
     * Compares two annotations by checking if their URIs and body URIs have the same path.
     * 
     * @param previousResource
     *            one annotation
     * @param resource
     *            second annotation
     * @return true if they are equal, false otherwise
     */
    private boolean annotationsAreEqual(Annotation previousResource, Annotation resource) {
        boolean pathOk = previousResource.getPath().equals(resource.getPath());
        String bodyPath1 = previousResource.getResearchObject().getUri()
                .relativize(previousResource.getBody().getUri()).getPath();
        String bodyPath2 = resource.getResearchObject().getUri().relativize(resource.getBody().getUri()).getPath();
        boolean bodyOk = bodyPath1.equals(bodyPath2);
        return pathOk && bodyOk;
    }


    /**
     * Compare two folders. TODO
     * 
     * @param previousResource
     * @param resource
     * @return
     */
    private boolean foldersAreEqual(Folder previousResource, Folder resource) {
        // TODO Auto-generated method stub
        return true;
    }
}
