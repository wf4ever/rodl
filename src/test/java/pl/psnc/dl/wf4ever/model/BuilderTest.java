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
import pl.psnc.dl.wf4ever.model.ROEVO.ImmutableResearchObject;

/**
 * Test class for builder.
 * 
 * @author pejot
 * 
 */
public class BuilderTest extends BaseTest {

    URI exampleUri = URI.create("http://www.example.com/exampleUri/");
    DateTime now;
    UserMetadata userM;


    @Before
    public void setUp()
            throws Exception {
        super.setUp();
        now = DateTime.now();
        userM = new UserMetadata("janek", "Jane Kowalski", Role.AUTHENTICATED);
    }


    @Test
    public void testBuilder() {
        Builder builder2 = new Builder(userProfile, dataset, false);
        Assert.assertEquals(builder2.getUser(), userProfile);
        Assert.assertEquals(builder2.getDataset(), dataset);
        Assert.assertFalse(builder2.isUseTransactions());
    }


    @Test
    public void testBuildThing() {
        Thing thing = builder.buildThing(exampleUri);
        Assert.assertEquals(thing.getBuilder(), builder);
        Assert.assertEquals(thing.getUri(), exampleUri);
        Assert.assertNotNull(thing.getName());
        Assert.assertNull(thing.getCopyAuthor());
        Assert.assertNull(thing.getCopyOf());
        Assert.assertNull(thing.getCopyDateTime());
        Assert.assertNull(thing.getCreated());
        Assert.assertNull(thing.getCreator());
        Assert.assertNull(thing.getModified());
    }


    @Test
    public void testBuildResearchObject() {
        ResearchObject researchObject = builder.buildResearchObject(exampleUri);
        Assert.assertNull(researchObject.getCreated());
        Assert.assertNull(researchObject.getCreator());
    }


    @Test
    public void testBuildImmutableResearchObject() {
        ImmutableResearchObject immutableResearchObject = builder.buildImmutableResearchObject(researchObject.getUri()
                .resolve("immutable"));
        Assert.assertNull(immutableResearchObject.getCopyOf());
        Assert.assertNull(immutableResearchObject.getCopyAuthor());
        Assert.assertNull(immutableResearchObject.getCopyDateTime());
    }


    @Test
    public void testBuildManifest() {
        Manifest manifest = builder.buildManifest(exampleUri, researchObject);
        Assert.assertEquals(manifest.getResearchObject(), researchObject);
    }


    @Test
    public void testBuildManifest2() {
        Manifest manifest = builder.buildManifest(exampleUri, researchObject, userProfile, now);
        Assert.assertEquals(manifest.getResearchObject(), researchObject);
        Assert.assertEquals(manifest.getCreator(), userProfile);
        Assert.assertEquals(manifest.getCreated(), now);
    }


    @Test
    public void testBuildAggregatedResource() {
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(
            exampleUri.resolve("aggregated-resource"), researchObject, userProfile, now);
        Assert.assertEquals(aggregatedResource.getCreated(), now);
        Assert.assertEquals(aggregatedResource.getCreator(), userProfile);
        Assert.assertEquals(aggregatedResource.getResearchObject(), researchObject);
    }


    @Test
    public void testBuildProxy() {
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(
            exampleUri.resolve("aggregated-resource"), researchObject, userProfile, DateTime.now());
        Proxy proxy = builder.buildProxy(exampleUri, aggregatedResource, researchObject);
        Assert.assertEquals(proxy.getProxyFor(), aggregatedResource);
        Assert.assertEquals(proxy.getProxyIn(), researchObject);
    }


    @Test
    public void testBuildAnnotation() {
        Thing body = builder.buildThing(exampleUri.resolve("body"));
        Set<Thing> targets = new HashSet<Thing>();
        targets.add(researchObject);
        Annotation annotation = builder.buildAnnotation(exampleUri, researchObject, body, targets);
        Assert.assertEquals(annotation.getResearchObject(), researchObject);
        Assert.assertEquals(annotation.getBody(), body);
    }


    @Test
    public void testBuildAnnotation2() {
        Thing body = builder.buildThing(exampleUri.resolve("body"));
        Set<Thing> targets = new HashSet<Thing>();
        targets.add(researchObject);
        Annotation annotation = builder.buildAnnotation(exampleUri, researchObject, body, targets, userM, now);
        Assert.assertEquals(annotation.getResearchObject(), researchObject);
        Assert.assertEquals(annotation.getBody(), body);
        Assert.assertEquals(annotation.getCreator(), userM);
        Assert.assertEquals(annotation.getCreated(), now);
    }


    @Test
    public void testBuildAnnotation3() {
        Thing body = builder.buildThing(exampleUri.resolve("body"));
        Annotation annotation = builder.buildAnnotation(exampleUri, researchObject, body, researchObject, userM, now);
        Assert.assertEquals(annotation.getResearchObject(), researchObject);
        Assert.assertEquals(annotation.getBody(), body);
        Assert.assertEquals(annotation.getCreator(), userM);
        Assert.assertEquals(annotation.getCreated(), now);
    }


    @Test
    public void testBuildResource() {
        Resource resource = builder.buildResource(exampleUri, researchObject, userM, now);
        Assert.assertEquals(resource.getResearchObject(), researchObject);
        Assert.assertEquals(resource.getCreator(), userM);
        Assert.assertEquals(resource.getCreated(), now);
    }


    @Test
    public void testBuildFolder() {
        Folder folder = builder.buildFolder(exampleUri, researchObject, userM, now, null);
        Assert.assertEquals(folder.getResearchObject(), researchObject);
        Assert.assertEquals(folder.getCreator(), userM);
        Assert.assertEquals(folder.getCreated(), now);
    }


    @Test
    public void testBuildFolderResourceMap() {
        Folder folder = builder.buildFolder(exampleUri.resolve("folder"), researchObject, userM, now, null);
        FolderResourceMap folderResourceMap = builder.buildFolderResourceMap(exampleUri, folder);
        Assert.assertEquals(folderResourceMap.getFolder(), folder);
    }


    @Test
    public void testBuildFolderEntry() {
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(
            exampleUri.resolve("aggregated-resource"), researchObject, userProfile, now);
        Folder folder = builder.buildFolder(exampleUri.resolve("folder"), researchObject, userM, now, null);
        FolderEntry folderEntry = builder.buildFolderEntry(exampleUri, aggregatedResource, folder, "entry");
        Assert.assertEquals(folderEntry.getName(), exampleUri.getPath().replaceAll("/", ""));
        Assert.assertEquals(folderEntry.getEntryName(), "entry");
        Assert.assertEquals(folderEntry.getFolder(), folder);
        Assert.assertEquals(folderEntry.getProxyIn(), folder);
    }
}
