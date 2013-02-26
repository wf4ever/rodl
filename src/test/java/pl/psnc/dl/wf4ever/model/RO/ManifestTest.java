package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

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
    public void setUp() {
        super.setUp();
        manifestUri = researchObject.getUri().resolve("manifest");
        manifest = builder.buildManifest(manifestUri, researchObject);
    }


    @Test
    public void testConstructor() {
        Manifest manifest = new Manifest(userProfile, manifestUri, researchObject);
        Manifest manifest2 = new Manifest(userProfile, null, false, manifestUri, researchObject);
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
        manifest.save();
        manifest.delete();
        Assert.assertNull(manifest.getGraphAsInputStream(RDFFormat.RDFXML));
    }


    @Test
    public void testCreate() {
        Model model = ModelFactory.createDefaultModel();
        model.read(manifest.getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(manifest.getUri().toString());
        Assert.assertTrue(r.hasProperty(RDF.type, RO.Manifest));
        r = model.getResource(researchObject.getUri().toString());
        Assert.assertTrue(r.hasProperty(RDF.type, RO.ResearchObject));
    }


    @Test
    public void testCopy() {
        Manifest m2 = manifest.copy(builder, researchObject2);
        Assert.assertEquals(manifest.getUri().relativize(researchObject.getUri()),
            m2.getUri().relativize(researchObject2.getUri()));
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
        manifest.saveRoResourceClass(builder.buildAggregatedResource(aggregatedResourceUri, researchObject,
            userProfile, DateTime.now()));
        Model model = ModelFactory.createDefaultModel();
        model.read(manifest.getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(aggregatedResourceUri.toString());
        Assert.assertTrue(r.hasProperty(RO.checksum));
        Assert.assertTrue(r.hasProperty(RO.filesize));
    }

}
