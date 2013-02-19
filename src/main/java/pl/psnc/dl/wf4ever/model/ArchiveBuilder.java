package pl.psnc.dl.wf4ever.model;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.RDF.Thing;

/**
 * The builder setting properties of an archived resource.
 * 
 * @author piotrekhol
 * 
 */
public class ArchiveBuilder implements EvoBuilder {

    @Override
    public void setFrozenAt(Thing resource, DateTime time) {
        resource.setArchivedAt(time);
    }


    @Override
    public void setFrozenBy(Thing resource, UserMetadata user) {
        resource.setArchivedBy(user);
    }


    @Override
    public void setIsCopyOf(Thing resource, Thing original) {
        resource.setArchiveOf(original);
    }

}
