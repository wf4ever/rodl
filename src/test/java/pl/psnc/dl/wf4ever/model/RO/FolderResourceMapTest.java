package pl.psnc.dl.wf4ever.model.RO;

import java.io.InputStream;
import java.net.URI;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.vocabulary.ORE;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class FolderResourceMapTest extends BaseTest {

    private URI folderResourceMapUri;
    private FolderBuilder folderBuilder;
    private Folder folder;
    private FolderResourceMap folderResourceMap;


    @Override
    @Before
    public void setUp()
            throws Exception {
        super.setUp();
        folderResourceMapUri = researchObject.getUri().resolve("folder-rm.ttl");
        Model model = FileManager.get().loadModel(folderResourceMapUri.toString(), folderResourceMapUri.toString(),
            "TURTLE");
        dataset.addNamedModel(folderResourceMapUri.toString(), model);
        folderBuilder = new FolderBuilder();
        folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject,
            folderResourceMapUri.resolve("folder"));
        folderResourceMap = builder.buildFolderResourceMap(folderResourceMapUri, folder);
    }


    @Test
    public void testConstructor()
            throws BadRequestException {
        folderResourceMap = new FolderResourceMap(userProfile, dataset, true, folder, folderResourceMapUri);
        Assert.assertEquals(folderResourceMap.getFolder(), folder);
    }


    @Test
    public void testSaveFolderEntryData()
            throws BadRequestException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(FolderBuilder.DEFAULT_FOLDER_PATH);
        researchObject.aggregate("new-resource", is, "text/plain");
        is = getClass().getClassLoader().getResourceAsStream("model/ro/folder_resource_map/folder_entry.rdf");
        FolderEntry fe = folder.createFolderEntry(is);
        folderResourceMap.saveFolderEntryData(fe);
        Model model = ModelFactory.createDefaultModel();
        model.read(folderResourceMap.getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(fe.getUri().toString());
        Assert.assertTrue(r.hasProperty(RDF.type, RO.FolderEntry));
        r = model.getResource(fe.getProxyFor().getUri().toString());
        Assert.assertTrue(r.hasProperty(ORE.isAggregatedBy, model.getResource(fe.getProxyIn().getUri().toString())));
        r = model.getResource(fe.getProxyIn().getUri().toString());
        Assert.assertTrue(r.hasProperty(ORE.aggregates, model.getResource(fe.getProxyFor().getUri().toString())));
    }


    @Test
    public void testGenerateResourceUri() {
        Assert.assertNull(FolderResourceMap.generateResourceMapUri(builder.buildFolder(null, researchObject, null,
            null, null)));
        Assert.assertEquals(URI.create("/folder.rdf"), FolderResourceMap.generateResourceMapUri(builder.buildFolder(
            URI.create(""), researchObject, null, null, null)));
        Assert.assertEquals(URI.create("/folder.rdf"), FolderResourceMap.generateResourceMapUri(builder.buildFolder(
            URI.create("/folder"), researchObject, null, null, null)));
        Assert.assertEquals(URI.create("folder.rdf"), FolderResourceMap.generateResourceMapUri(builder.buildFolder(
            URI.create("folder"), researchObject, null, null, null)));
        Assert.assertEquals(URI.create("folder/folder.rdf"), FolderResourceMap.generateResourceMapUri(builder
                .buildFolder(URI.create("folder/"), researchObject, null, null, null)));
        Assert.assertEquals(URI.create("http://www.example.org/folder.rdf"), FolderResourceMap
                .generateResourceMapUri(builder.buildFolder(URI.create("http://www.example.org"), researchObject, null,
                    null, null)));
        Assert.assertEquals(URI.create("http://www.example.org/folder.rdf"), FolderResourceMap
                .generateResourceMapUri(builder.buildFolder(URI.create("http://www.example.org/"), researchObject,
                    null, null, null)));
        Assert.assertEquals(URI.create("http://www.example.org/resurce.rdf"), FolderResourceMap
                .generateResourceMapUri(builder.buildFolder(URI.create("http://www.example.org/resurce"),
                    researchObject, null, null, null)));
        Assert.assertEquals(URI.create("http://www.example.org/rosource/rosource.rdf"), FolderResourceMap
                .generateResourceMapUri(builder.buildFolder(URI.create("http://www.example.org/rosource/"),
                    researchObject, null, null, null)));
    }


    @Test
    public void testExtractFolderEntries()
            throws BadRequestException {
        Assert.assertEquals(3, folderResourceMap.extractFolderEntries().size());
    }


    @Test
    public void testExtractResearchObject()
            throws BadRequestException {
        Assert.assertEquals(researchObject, folderResourceMap.extractResearchObject());
    }


    @Test
    public void testSave()
            throws BadRequestException {
        folderResourceMap.save();
        Model model = ModelFactory.createDefaultModel();
        model.read(folderResourceMap.getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(folderResourceMap.getUri().toString());
        Assert.assertTrue(r.hasProperty(ORE.describes, model.getResource(folder.getUri().toString())));
        Assert.assertTrue(r.hasProperty(RDF.type, ORE.ResourceMap));
    }
}
