package pl.psnc.dl.wf4ever.model.ROEVO;

import java.net.URI;

import pl.psnc.dl.wf4ever.common.db.EvoType;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

import com.hp.hpl.jena.query.Dataset;

public abstract class EvoInfo extends AggregatedResource {

    protected EvoType evoType;


    public EvoInfo(UserMetadata user, Dataset dataset, boolean useTransactions, ResearchObject researchObject, URI uri) {
        super(user, dataset, useTransactions, researchObject, uri);
    }


    public EvoInfo(UserMetadata user, ResearchObject researchObject, URI uri) {
        super(user, researchObject, uri);
    }


    public EvoType getEvoType() {
        return this.evoType;
    }


    public void setEvoType(EvoType evoType) {
        this.evoType = evoType;
    }


    public abstract void load();

}