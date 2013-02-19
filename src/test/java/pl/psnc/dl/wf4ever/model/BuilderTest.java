package pl.psnc.dl.wf4ever.model;

import java.net.URI;
import java.util.HashSet;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Test;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.ORE.Proxy;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.Manifest;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.model.ROEVO.SnapshotResearchObject;

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
    private UserMetadata user = new UserMetadata("jank", "Jan Kowalski", Role.AUTHENTICATED);
    private Boolean useTransaction = false;
    Builder builder = new Builder(user, dataset, useTransaction);
    URI exampleUri = URI.create("http://www.example.com/exampleUri/");


    @Test
    public void testBuilder() {
        Assert.assertEquals(builder.getUser(), user);
        Assert.assertEquals(builder.getDataset(), dataset);
    }


    @Test
    public void testBuildThing() {
        Thing thing = builder.buildThing(exampleUri);
        Assert.assertEquals(thing.getBuilder(), builder);
        Assert.assertEquals(thing.getUri(), exampleUri);
        Assert.assertEquals(thing.getContributors(), new HashSet());
        Assert.assertNotNull(thing.getFilename());
        Assert.assertNull(thing.getArchivedAt());
        Assert.assertNull(thing.getArchivedBy());
        Assert.assertNull(thing.getSnapshottedAt());
        Assert.assertNull(thing.getSnapshottedBy());
        Assert.assertNull(thing.getCreated());
        Assert.assertNull(thing.getCreator());
        Assert.assertNull(thing.getModified());
    }


    @Test
    public void testBuildResearchObject() {
        ResearchObject researchObject = builder.buildResearchObject(exampleUri);
        researchObject.getCreated();
        Assert.assertNull(researchObject.getCreator());
    }


    @Test
    public void testBuildSnapshotResearchObject() {
        ResearchObject researchObject = builder.buildResearchObject(exampleUri);
        SnapshotResearchObject snapshotResearchObject = builder.buildSnapshotResearchObject(
            exampleUri.resolve("snapshot"), researchObject);
        Assert.assertNull(snapshotResearchObject.getSnapshottedAt());
        Assert.assertNull(snapshotResearchObject.getSnapshottedBy());
    }


    @Test
    public void testBuildArchiveResearchObject() {
        ResearchObject researchObject = builder.buildResearchObject(exampleUri);
        SnapshotResearchObject snapshotResearchObject = builder.buildSnapshotResearchObject(
            exampleUri.resolve("archive"), researchObject);
        Assert.assertNull(snapshotResearchObject.getArchivedAt());
        Assert.assertNull(snapshotResearchObject.getArchivedBy());
    }


    @Test
    public void testBuildManifest() {
        ResearchObject researchObject = builder.buildResearchObject(exampleUri.resolve("research-object"));
        Manifest manifest = builder.buildManifest(exampleUri, researchObject);
        Assert.assertEquals(manifest.getResearchObject(), researchObject);
    }


    @Test
    public void testBuildManifest2() {
        ResearchObject researchObject = builder.buildResearchObject(exampleUri.resolve("research-object"));
        DateTime now = DateTime.now();
        Manifest manifest = builder.buildManifest(exampleUri, researchObject, user, now);
        Assert.assertEquals(manifest.getResearchObject(), researchObject);
        Assert.assertEquals(manifest.getCreator(), user);
        Assert.assertEquals(manifest.getCreated(), now);
    }


    @Test
    public void testBuildAggregatedResource() {
        ResearchObject researchObject = builder.buildResearchObject(exampleUri);
        DateTime now = DateTime.now();
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(
            exampleUri.resolve("aggregated-resource"), researchObject, user, now);
        Assert.assertEquals(aggregatedResource.getCreated(), now);
        Assert.assertEquals(aggregatedResource.getCreator(), user);
        Assert.assertEquals(aggregatedResource.getResearchObject(), researchObject);
    }


    @Test
    public void testBuildProxy() {
        ResearchObject researchObject = builder.buildResearchObject(exampleUri.resolve("research-object"));
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(
            exampleUri.resolve("aggregated-resource"), researchObject, user, DateTime.now());
        Proxy proxy = builder.buildProxy(exampleUri, aggregatedResource, researchObject);
        Assert.assertEquals(proxy.getProxyFor(), aggregatedResource);
        Assert.assertEquals(proxy.getProxyIn(), researchObject);
    }


    public void testBuildAnnotation() {

    }

}
