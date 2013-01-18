package pl.psnc.dl.wf4ever.model;

import java.net.URI;

import junit.framework.Assert;

import org.junit.Test;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
import pl.psnc.dl.wf4ever.model.RDF.Thing;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * Test class for builder.
 * 
 * @author pejot
 * 
 */
public class BuilderTest {

    private Dataset dataset = TDBFactory.createDataset();
    private UserMetadata userProfile = new UserMetadata("jank", "Jan Kowalski", Role.AUTHENTICATED);
    private Boolean useTransaction = false;
    Builder builder = new Builder(userProfile, dataset, useTransaction);
    URI exampleUri = URI.create("http://www.example.com/exampleUri/");


    @Test
    public void testBuilder() {
        Assert.assertEquals(builder.getUser(), userProfile);
    }


    @Test
    public void testBuildThing() {
        Thing testThing = builder.buildThing(exampleUri);
        Assert.assertEquals(testThing.getBuilder(), builder);
        Assert.assertEquals(testThing.getUri(), exampleUri);
    }
}
