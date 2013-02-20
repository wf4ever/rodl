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
        SnapshotResearchObject snapshotResearchObject = builder.buildSnapshotResearchObject(
            exampleUri.resolve("snapshot"), fakeResearchObject);
        Assert.assertNull(snapshotResearchObject.getSnapshottedAt());
        Assert.assertNull(snapshotResearchObject.getSnapshottedBy());
    }


    @Test
    public void testBuildArchiveResearchObject() {
        SnapshotResearchObject snapshotResearchObject = builder.buildSnapshotResearchObject(
            exampleUri.resolve("archive"), fakeResearchObject);
        Assert.assertNull(snapshotResearchObject.getArchivedAt());
        Assert.assertNull(snapshotResearchObject.getArchivedBy());
    }


    @Test
    public void testBuildManifest() {
        Manifest manifest = builder.buildManifest(exampleUri, fakeResearchObject);
        Assert.assertEquals(manifest.getResearchObject(), fakeResearchObject);
    }


    @Test
    public void testBuildManifest2() {
        Manifest manifest = builder.buildManifest(exampleUri, fakeResearchObject, user, now);
        Assert.assertEquals(manifest.getResearchObject(), fakeResearchObject);
        Assert.assertEquals(manifest.getCreator(), user);
        Assert.assertEquals(manifest.getCreated(), now);
    }


    @Test
    public void testBuildAggregatedResource() {
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(
            exampleUri.resolve("aggregated-resource"), fakeResearchObject, user, now);
        Assert.assertEquals(aggregatedResource.getCreated(), now);
        Assert.assertEquals(aggregatedResource.getCreator(), user);
        Assert.assertEquals(aggregatedResource.getResearchObject(), fakeResearchObject);
    }


    @Test
    public void testBuildProxy() {
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(
            exampleUri.resolve("aggregated-resource"), fakeResearchObject, user, DateTime.now());
        Proxy proxy = builder.buildProxy(exampleUri, aggregatedResource, fakeResearchObject);
        Assert.assertEquals(proxy.getProxyFor(), aggregatedResource);
        Assert.assertEquals(proxy.getProxyIn(), fakeResearchObject);
    }


    @Test
    public void testBuildAnnotation() {
        Thing body = builder.buildThing(exampleUri.resolve("body"));
        Set<Thing> targets = new HashSet<Thing>();
        targets.add(fakeResearchObject);
        Annotation annotation = builder.buildAnnotation(fakeResearchObject, exampleUri, body, targets);
        Assert.assertEquals(annotation.getResearchObject(), fakeResearchObject);
        Assert.assertEquals(annotation.getBody(), body);
    }


    @Test
    public void testBuildAnnotation2() {
        Thing body = builder.buildThing(exampleUri.resolve("body"));
        Set<Thing> targets = new HashSet<Thing>();
        targets.add(fakeResearchObject);
        Annotation annotation = builder.buildAnnotation(fakeResearchObject, exampleUri, body, targets, userM, now);
        Assert.assertEquals(annotation.getResearchObject(), fakeResearchObject);
        Assert.assertEquals(annotation.getBody(), body);
        Assert.assertEquals(annotation.getCreator(), userM);
        Assert.assertEquals(annotation.getCreated(), now);
    }


    @Test
    public void testBuildAnnotation3() {
        Thing body = builder.buildThing(exampleUri.resolve("body"));
        Annotation annotation = builder.buildAnnotation(fakeResearchObject, exampleUri, body, fakeResearchObject, userM, now);
        Assert.assertEquals(annotation.getResearchObject(), fakeResearchObject);
        Assert.assertEquals(annotation.getBody(), body);
        Assert.assertEquals(annotation.getCreator(), userM);
        Assert.assertEquals(annotation.getCreated(), now);
    }


    @Test
    public void testBuildResource() {
        Resource resource = builder.buildResource(fakeResearchObject, exampleUri, userM, now);
        Assert.assertEquals(resource.getResearchObject(), fakeResearchObject);
        Assert.assertEquals(resource.getCreator(), userM);
        Assert.assertEquals(resource.getCreated(), now);
    }


    @Test
    public void testBuildFolder() {
        Folder folder = builder.buildFolder(fakeResearchObject, exampleUri, userM, now);
        Assert.assertEquals(folder.getResearchObject(), fakeResearchObject);
        Assert.assertEquals(folder.getCreator(), userM);
        Assert.assertEquals(folder.getCreated(), now);
    }


    @Test
    public void testBuildFolderResourceMap() {
        Folder folder = builder.buildFolder(fakeResearchObject, exampleUri.resolve("folder"), userM, now);
        FolderResourceMap folderResourceMap = builder.buildFolderResourceMap(exampleUri, folder);
        Assert.assertEquals(folderResourceMap.getFolder(), folder);
    }


    @Test
    public void testBuildFolderEntry() {
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(
            exampleUri.resolve("aggregated-resource"), fakeResearchObject, user, now);
        Folder folder = builder.buildFolder(fakeResearchObject, exampleUri.resolve("folder"), userM, now);
        FolderEntry folderEntry = builder.buildFolderEntry(exampleUri, aggregatedResource, folder, "entry");
        Assert.assertEquals(folderEntry.getFilename(), exampleUri.getPath().replaceAll("/", ""));
        Assert.assertEquals(folderEntry.getEntryName(), "entry");
        Assert.assertEquals(folderEntry.getFolder(), folder);
        Assert.assertEquals(folderEntry.getProxyIn(), folder);
    }
}
