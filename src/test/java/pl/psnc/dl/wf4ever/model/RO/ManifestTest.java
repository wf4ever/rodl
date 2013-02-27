package pl.psnc.dl.wf4ever.model.RO;

import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.vocabulary.AO;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class ManifestTest extends BaseTest {

    private URI manifestUri;
    private Manifest manifest;


    @Override
    @Before
    public void setUp() {
        super.setUp();
        manifestUri = researchObject.getUri().resolve("manifest");
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
        Folder folder = builder.buildFolder(researchObject, folderUri, userProfile, DateTime.now());
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
    public void testExtractAggreagtedResource() {
        //TODO totally confused
        assert false;
    }


    @Test
    public void testExtractResources() {
        Manifest m = researchObject.getManifest();
        int start = m.extractResources().size();
        researchObject.aggregate(URI.create("http://example.com/external/"));
        Assert.assertEquals(start, m.extractResources().size());
    }


    @Test
    public void extractFolders()
            throws BadRequestException {
        Manifest m = researchObject.getManifest();
        int start = m.extractFolders().size();
        URI folderUri = researchObject.getUri().resolve("new-folder-uri");
        FolderBuilder folderBuilder = new FolderBuilder();
        Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
        Assert.assertEquals(start + 1, m.extractFolders().size());
    }


    @Test
    public void testExtractAnnotations()
            throws BadRequestException {
        Manifest m = researchObject.getManifest();
        int start = m.extractAnnotations().size();
        InputStream is = getClass().getClassLoader().getResourceAsStream(FolderBuilder.DEFAULT_FOLDER_PATH);
        researchObject.annotate(is);
        is = getClass().getClassLoader().getResourceAsStream(FolderBuilder.DEFAULT_FOLDER_PATH);
        researchObject.annotate(is);
        is = getClass().getClassLoader().getResourceAsStream(FolderBuilder.DEFAULT_FOLDER_PATH);
        researchObject.annotate(is);
        Assert.assertEquals(start + 3, m.extractAnnotations().size());
    }


    @Test
    public void testSaveAnnotationData() {
        URI annotationUri = manifest.getUri().resolve("new-annotation");
        Set<Thing> targets = new HashSet<Thing>();
        targets.add(manifest);
        manifest.saveAnnotationData(builder.buildAnnotation(researchObject, annotationUri, manifest, targets));
        Model model = ModelFactory.createDefaultModel();
        model.read(manifest.getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(annotationUri.toString());
        Assert.assertTrue(r.hasProperty(RO.annotatesAggregatedResource));
        Assert.assertTrue(r.hasProperty(AO.body));
    }


    @Test
    public void testSaveDuplicatedAnnotationData() {
        URI annotationUri = manifest.getUri().resolve("new-annotation");
        Set<Thing> targets = new HashSet<Thing>();
        targets.add(manifest);
        manifest.saveAnnotationData(builder.buildAnnotation(researchObject, annotationUri, manifest, targets));
        Model model = ModelFactory.createDefaultModel();
        model.read(manifest.getGraphAsInputStream(RDFFormat.RDFXML), null);
        manifest.saveAnnotationData(builder.buildAnnotation(researchObject, annotationUri, manifest, targets));

        Assert.assertEquals(1, model.listSubjectsWithProperty(RO.annotatesAggregatedResource).toList().size());
        Resource r = model.getResource(annotationUri.toString());
        Assert.assertEquals(1, r.listProperties(RO.annotatesAggregatedResource));
        Assert.assertEquals(1, r.listProperties(AO.body));

    }
}
