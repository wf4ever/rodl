package pl.psnc.dl.wf4ever.model.ROEVO;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RDF.Thing;

import com.hp.hpl.jena.query.Dataset;

public class Change extends Thing {

    public enum ChangeType {
        ADDITION,
        MODIFICATION,
        REMOVAL
    }


    private ChangeSpecification changeSpecification;

    private AggregatedResource resource;

    private ChangeType changeType;


    public Change(UserMetadata user, Dataset dataset, Boolean useTransactions, URI uri,
            ChangeSpecification changeSpecification) {
        super(user, dataset, useTransactions, uri);
        this.changeSpecification = changeSpecification;
    }


    public static Change create(Builder builder, ChangeSpecification changeSpecification, AggregatedResource resource,
            ChangeType type) {
        URI uri = UriBuilder.fromUri(changeSpecification.getUri()).path("changes/" + UUID.randomUUID().toString())
                .build();
        Change change = builder.buildChange(uri, changeSpecification, resource, type);
        change.save();
        return change;
    }


    @Override
    public void save() {
        super.save();
        this.changeSpecification.getResearchObject().getImmutableEvoInfo().saveChange(this);
    }


    public ChangeSpecification getChangeSpecification() {
        return changeSpecification;
    }


    public AggregatedResource getResource() {
        return resource;
    }


    public void setResource(AggregatedResource resource) {
        this.resource = resource;
    }


    public ChangeType getChangeType() {
        return changeType;
    }


    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

}
