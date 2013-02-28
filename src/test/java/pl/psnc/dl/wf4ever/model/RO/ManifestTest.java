package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
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
    public void setUp()
            throws Exception {
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

}
