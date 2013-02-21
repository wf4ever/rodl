package pl.psnc.dl.wf4ever.model;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.ORE.Proxy;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.Folder;
import pl.psnc.dl.wf4ever.model.RO.FolderEntry;
import pl.psnc.dl.wf4ever.model.RO.FolderResourceMap;
import pl.psnc.dl.wf4ever.model.RO.Manifest;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.model.RO.Resource;
import pl.psnc.dl.wf4ever.model.ROEVO.SnapshotResearchObject;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * Test class for builder.
 * 
 * @author pejot
 * 
 */
public class BuilderTest extends BaseTest {

    private Dataset dataset = TDBFactory.createDataset();
    private UserMetadata user = new UserMetadata("jank", "Jan Kowalski", Role.AUTHENTICATED);
    private Boolean useTransaction = false;
    Builder builder = new Builder(user, dataset, useTransaction);
    URI exampleUri = URI.create("http://www.example.com/exampleUri/");
    DateTime now;
    UserMetadata userM;


    @Before
    public void setUp() {
        super.setUp();
        DateTime.now();
        userM = new UserMetadata("janek", "Jane Kowalski", Role.AUTHENTICATED);
    }


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
        Assert.assertNotNull(thing.getName());
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
        SnapshotResearchObject snapshotResearchObject = builder.buildSnapshotResearchObject(
            exampleUri.resolve("snapshot"), messRO);
        Assert.assertNull(snapshotResearchObject.getSnapshottedAt());
        Assert.assertNull(snapshotResearchObject.getSnapshottedBy());
    }


    @Test
    public void testBuildArchiveResearchObject() {
        SnapshotResearchObject snapshotResearchObject = builder.buildSnapshotResearchObject(
            exampleUri.resolve("archive"), messRO);
        Assert.assertNull(snapshotResearchObject.getArchivedAt());
        Assert.assertNull(snapshotResearchObject.getArchivedBy());
    }


    @Test
    public void testBuildManifest() {
        Manifest manifest = builder.buildManifest(exampleUri, messRO);
        Assert.assertEquals(manifest.getResearchObject(), messRO);
    }


    @Test
    public void testBuildManifest2() {
        Manifest manifest = builder.buildManifest(exampleUri, messRO, user, now);
        Assert.assertEquals(manifest.getResearchObject(), messRO);
        Assert.assertEquals(manifest.getCreator(), user);
        Assert.assertEquals(manifest.getCreated(), now);
    }


    @Test
    public void testBuildAggregatedResource() {
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(
            exampleUri.resolve("aggregated-resource"), messRO, user, now);
        Assert.assertEquals(aggregatedResource.getCreated(), now);
        Assert.assertEquals(aggregatedResource.getCreator(), user);
        Assert.assertEquals(aggregatedResource.getResearchObject(), messRO);
    }


    @Test
    public void testBuildProxy() {
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(
            exampleUri.resolve("aggregated-resource"), messRO, user, DateTime.now());
        Proxy proxy = builder.buildProxy(exampleUri, aggregatedResource, messRO);
        Assert.assertEquals(proxy.getProxyFor(), aggregatedResource);
        Assert.assertEquals(proxy.getProxyIn(), messRO);
    }


    @Test
    public void testBuildAnnotation() {
        Thing body = builder.buildThing(exampleUri.resolve("body"));
        Set<Thing> targets = new HashSet<Thing>();
        targets.add(messRO);
        Annotation annotation = builder.buildAnnotation(messRO, exampleUri, body, targets);
        Assert.assertEquals(annotation.getResearchObject(), messRO);
        Assert.assertEquals(annotation.getBody(), body);
    }


    @Test
    public void testBuildAnnotation2() {
        Thing body = builder.buildThing(exampleUri.resolve("body"));
        Set<Thing> targets = new HashSet<Thing>();
        targets.add(messRO);
        Annotation annotation = builder.buildAnnotation(messRO, exampleUri, body, targets, userM, now);
        Assert.assertEquals(annotation.getResearchObject(), messRO);
        Assert.assertEquals(annotation.getBody(), body);
        Assert.assertEquals(annotation.getCreator(), userM);
        Assert.assertEquals(annotation.getCreated(), now);
    }


    @Test
    public void testBuildAnnotation3() {
        Thing body = builder.buildThing(exampleUri.resolve("body"));
        Annotation annotation = builder.buildAnnotation(messRO, exampleUri, body, messRO, userM, now);
        Assert.assertEquals(annotation.getResearchObject(), messRO);
        Assert.assertEquals(annotation.getBody(), body);
        Assert.assertEquals(annotation.getCreator(), userM);
        Assert.assertEquals(annotation.getCreated(), now);
    }


    @Test
    public void testBuildResource() {
        Resource resource = builder.buildResource(messRO, exampleUri, userM, now);
        Assert.assertEquals(resource.getResearchObject(), messRO);
        Assert.assertEquals(resource.getCreator(), userM);
        Assert.assertEquals(resource.getCreated(), now);
    }


    @Test
    public void testBuildFolder() {
        Folder folder = builder.buildFolder(messRO, exampleUri, userM, now);
        Assert.assertEquals(folder.getResearchObject(), messRO);
        Assert.assertEquals(folder.getCreator(), userM);
        Assert.assertEquals(folder.getCreated(), now);
    }


    @Test
    public void testBuildFolderResourceMap() {
        Folder folder = builder.buildFolder(messRO, exampleUri.resolve("folder"), userM, now);
        FolderResourceMap folderResourceMap = builder.buildFolderResourceMap(exampleUri, folder);
        Assert.assertEquals(folderResourceMap.getFolder(), folder);
    }


    @Test
    public void testBuildFolderEntry() {
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(
            exampleUri.resolve("aggregated-resource"), messRO, user, now);
        Folder folder = builder.buildFolder(messRO, exampleUri.resolve("folder"), userM, now);
        FolderEntry folderEntry = builder.buildFolderEntry(exampleUri, aggregatedResource, folder, "entry");
        Assert.assertEquals(folderEntry.getName(), exampleUri.getPath().replaceAll("/", ""));
        Assert.assertEquals(folderEntry.getEntryName(), "entry");
        Assert.assertEquals(folderEntry.getFolder(), folder);
        Assert.assertEquals(folderEntry.getProxyIn(), folder);
    }
}
