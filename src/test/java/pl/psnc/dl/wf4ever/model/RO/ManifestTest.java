package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.AbstractUnitTest;
import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.vocabulary.AO;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class ManifestTest extends AbstractUnitTest {

    private URI manifestUri;
    private Manifest manifest;


    @Override
    @Before
    public void setUp()
            throws Exception {
        super.setUp();
        manifestUri = URI.create(MANIFEST);
        manifest = builder.buildManifest(manifestUri, researchObject);
    }


    @Test
    public void testConstructor() {
        new Manifest(userProfile, null, false, manifestUri, researchObject);
    }


    @Test
    public void testSave() {
        manifest.save();
        Model model = ModelFactory.createDefaultModel();
        model.read(manifest.getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(manifest.getUri().toString());
        Assert.assertTrue(r.hasProperty(RDF.type, RO.Manifest));
        r = model.getResource(researchObject.getUri().toString());
        Assert.assertTrue(r.hasProperty(RDF.type, RO.ResearchObject));
    }


    @Test
    public void testDelete() {
        Manifest manifest2 = Manifest.create(builder, manifestUri, researchObject);
        manifest2.delete();
        Assert.assertNull(manifest2.getGraphAsInputStream(RDFFormat.RDFXML));
    }


    @Test
    public void testCreate() {
        Manifest manifest2 = Manifest.create(builder, manifestUri, researchObject);
        Model model = ModelFactory.createDefaultModel();
        model.read(manifest2.getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(manifest2.getUri().toString());
        Assert.assertTrue(r.hasProperty(RDF.type, RO.Manifest));
        r = model.getResource(researchObject.getUri().toString());
        Assert.assertTrue(r.hasProperty(RDF.type, RO.ResearchObject));
    }


    @Test
    public void testCopy() {
        Manifest m2 = manifest.copy(builder, researchObject2);
        Assert.assertEquals(researchObject.getUri().relativize(manifest.getUri()),
            researchObject2.getUri().relativize(m2.getUri()));
    }


    @Test
    public void testSaveRoResourceClass() {
        URI aggregatedResourceUri = researchObject.getUri().resolve("new-agg-res");
        manifest.saveRoResourceClass(builder.buildAggregatedResource(aggregatedResourceUri, researchObject,
            userProfile, DateTime.now()));
        Model model = ModelFactory.createDefaultModel();
        model.read(manifest.getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(aggregatedResourceUri.toString());
        Assert.assertTrue(r.hasProperty(RDF.type, RO.Resource));
    }


    @Test
    public void testDeleteRoResourceClass() {
        URI aggregatedResourceUri = researchObject.getUri().resolve("new-agg-res");
        AggregatedResource resource = builder.buildAggregatedResource(aggregatedResourceUri, researchObject,
            userProfile, DateTime.now());
        manifest.saveRoResourceClass(resource);
        manifest.deleteResource(resource);
        Model model = ModelFactory.createDefaultModel();
        model.read(manifest.getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(aggregatedResourceUri.toString());
        Assert.assertFalse(r.hasProperty(RDF.type, RO.Resource));
    }


    @Test
    public void testSaveFolderData() {
        URI folderUri = researchObject.getUri().resolve("folder");
        Folder folder = builder.buildFolder(folderUri, researchObject, userProfile, DateTime.now(), null);
        manifest.saveFolderData(folder);
        Model model = ModelFactory.createDefaultModel();
        model.read(manifest.getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(folder.getUri().toString());
        Assert.assertTrue(r.hasProperty(RDF.type, RO.Folder));
    }


    @Test
    public void testSaveRoStats() {
        URI aggregatedResourceUri = researchObject.getUri().resolve("new-agg-res");
        AggregatedResource resource = builder.buildAggregatedResource(aggregatedResourceUri, researchObject,
            userProfile, DateTime.now());
        resource.setStats(new ResourceMetadata("foo", "foo", "xxx", 123, "MD5", null, "text/plain"));
        manifest.saveRoStats(resource);
        Model model = ModelFactory.createDefaultModel();
        model.read(manifest.getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(aggregatedResourceUri.toString());
        Assert.assertTrue(r.hasProperty(RO.checksum));
        Assert.assertTrue(r.hasProperty(RO.filesize));
    }


    @Test
    public void testExtractAggregatedResources() {
        Map<URI, AggregatedResource> expected = new HashMap<>();
        putAggregatedResource(expected, researchObject.getUri().resolve("a%20workflow.t2flow"));
        putAggregatedResource(expected, researchObject.getUri().resolve("afolder/"));
        putAggregatedResource(expected, researchObject.getUri().resolve("ann1"));
        Map<URI, pl.psnc.dl.wf4ever.model.RO.Resource> resources = new HashMap<>();
        Map<URI, Folder> folders = new HashMap<>();
        Map<URI, Annotation> annotations = new HashMap<>();
        Assert.assertEquals(expected, manifest.extractAggregatedResources(resources, folders, annotations));
    }


    private void putAggregatedResource(Map<URI, AggregatedResource> expected, URI uri) {
        expected.put(uri, builder.buildAggregatedResource(uri, researchObject, null, null));
    }


    @Test
    public void testExtractResources() {
        Map<URI, pl.psnc.dl.wf4ever.model.RO.Resource> expected = new HashMap<>();
        putResource(expected, researchObject.getUri().resolve("a%20workflow.t2flow"));
        Assert.assertEquals(expected, manifest.extractResources());
    }


    private void putResource(Map<URI, pl.psnc.dl.wf4ever.model.RO.Resource> expected, URI uri) {
        expected.put(uri, builder.buildResource(uri, researchObject, null, null));
    }


    @Test
    public void extractFolders()
            throws BadRequestException {
        Map<URI, Folder> expected = new HashMap<>();
        putFolder(expected, researchObject.getUri().resolve("afolder/"));
        Assert.assertEquals(expected, manifest.extractFolders());
    }


    private void putFolder(Map<URI, Folder> expected, URI uri) {
        expected.put(uri, builder.buildFolder(uri, researchObject, null, null, null));
    }


    @Test
    public void testExtractAnnotations()
            throws BadRequestException {
        Map<URI, Annotation> expected = new HashMap<>();
        putAnnotation(expected, researchObject.getUri().resolve("ann1"));
        Assert.assertEquals(expected, manifest.extractAnnotations());
    }


    private void putAnnotation(Map<URI, Annotation> expected, URI uri) {
        Thing body = builder.buildThing(researchObject.getUri().resolve("body"));
        Set<Thing> targets = new HashSet<>();
        targets.add(researchObject);
        expected.put(uri, builder.buildAnnotation(uri, researchObject, body, targets));
    }


    @Test
    public void testSaveAnnotationData() {
        Manifest manifest2 = builder.buildManifest(researchObject.getUri().resolve("newmanifest"), researchObject);
        URI annotationUri = manifest2.getUri().resolve("new-annotation");
        Set<Thing> targets = new HashSet<Thing>();
        targets.add(manifest2);
        manifest2.saveAnnotationData(builder.buildAnnotation(annotationUri, researchObject, manifest2, targets));
        Model model = ModelFactory.createDefaultModel();
        model.read(manifest2.getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(annotationUri.toString());
        Assert.assertTrue(r.hasProperty(RO.annotatesAggregatedResource));
        Assert.assertTrue(r.hasProperty(AO.body));
    }


    @Test
    public void testSaveDuplicatedAnnotationData() {
        Manifest manifest2 = builder.buildManifest(researchObject.getUri().resolve("newmanifest"), researchObject);
        URI annotationUri = manifest2.getUri().resolve("new-annotation");
        Set<Thing> targets = new HashSet<Thing>();
        targets.add(manifest2);
        manifest2.saveAnnotationData(builder.buildAnnotation(annotationUri, researchObject, manifest2, targets));
        Model model = ModelFactory.createDefaultModel();
        model.read(manifest2.getGraphAsInputStream(RDFFormat.RDFXML), null);
        manifest2.saveAnnotationData(builder.buildAnnotation(annotationUri, researchObject, manifest2, targets));

        Assert.assertEquals(1, model.listSubjectsWithProperty(RO.annotatesAggregatedResource).toList().size());
        Resource r = model.getResource(annotationUri.toString());
        Assert.assertEquals(1, r.listProperties(RO.annotatesAggregatedResource).toList().size());
        Assert.assertEquals(1, r.listProperties(AO.body).toList().size());

    }
}
