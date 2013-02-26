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
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

public class FolderResourceMapTest extends BaseTest {

    private URI folderResourceMapUri;
    private FolderBuilder folderBuilder;


    @Override
    @Before
    public void setUp() {
        super.setUp();
        folderResourceMapUri = researchObject.getUri().resolve("folder-resource-map");
        folderBuilder = new FolderBuilder();
    }


    @Test
    public void testConstructor()
            throws BadRequestException {
        Folder folder = folderBuilder.init(folderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject,
            folderResourceMapUri.resolve("folder"));
        FolderResourceMap folderResourceMap = new FolderResourceMap(userProfile, folder, folderResourceMapUri);
        Assert.assertEquals(folderResourceMap.getFolder(), folder);
        folderResourceMap = new FolderResourceMap(userProfile, dataset, true, folder, folderResourceMapUri);
        Assert.assertEquals(folderResourceMap.getFolder(), folder);
    }


    @Test
    public void testSaveFolderEntryData()
            throws BadRequestException {
        Folder folder = folderBuilder.init(folderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject,
            folderResourceMapUri.resolve("folder"));
        InputStream is = getClass().getClassLoader().getResourceAsStream(FolderBuilder.DEFAULT_FOLDER_PATH);
        researchObject.aggregate("new-resource", is, "text/plain");
        is = getClass().getClassLoader().getResourceAsStream("model/ro/folder_resource_map/folder_entry.rdf");
        FolderEntry fe = folder.createFolderEntry(is);
        FolderResourceMap folderResourceMap = builder.buildFolderResourceMap(folderResourceMapUri, folder);
        folderResourceMap.saveFolderEntryData(fe);
        Model model = ModelFactory.createDefaultModel();
        model.read(folderResourceMap.getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(fe.getUri().toString());
        Assert.assertTrue(r.hasProperty(RDF.type, RO.FolderEntry));
        r = model.getResource(fe.getProxyFor().getUri().toString());
        for (Statement s : r.listProperties().toList()) {
            System.out.println(s);
            System.out.println(fe.getProxyIn().getUri().toString());
        }
        Assert.assertTrue(r.hasProperty(ORE.isAggregatedBy, model.getResource(fe.getProxyIn().getUri().toString())));
        r = model.getResource(fe.getProxyIn().getUri().toString());
        Assert.assertTrue(r.hasProperty(ORE.aggregates, model.getResource(fe.getProxyFor().getUri().toString())));
    }


    @Test
    public void testGenerateResourceUri() {
        Assert.assertNull(FolderResourceMap.generateResourceMapUri(new Folder(userProfile, researchObject, null)));
        Assert.assertEquals(URI.create("/folder.rdf"),
            FolderResourceMap.generateResourceMapUri(new Folder(userProfile, researchObject, URI.create(""))));
        Assert.assertEquals(URI.create("/folder.rdf"),
            FolderResourceMap.generateResourceMapUri(new Folder(userProfile, researchObject, URI.create("/folder"))));
        Assert.assertEquals(URI.create("folder.rdf"),
            FolderResourceMap.generateResourceMapUri(new Folder(userProfile, researchObject, URI.create("folder"))));
        Assert.assertEquals(URI.create("folder/folder.rdf"),
            FolderResourceMap.generateResourceMapUri(new Folder(userProfile, researchObject, URI.create("folder/"))));
        Assert.assertEquals(URI.create("http://www.example.org/folder.rdf"), FolderResourceMap
                .generateResourceMapUri(new Folder(userProfile, researchObject, URI.create("http://www.example.org"))));
        Assert.assertEquals(URI.create("http://www.example.org/folder.rdf"), FolderResourceMap
                .generateResourceMapUri(new Folder(userProfile, researchObject, URI.create("http://www.example.org/"))));
        Assert.assertEquals(URI.create("http://www.example.org/resurce.rdf"), FolderResourceMap
                .generateResourceMapUri(new Folder(userProfile, researchObject, URI
                        .create("http://www.example.org/resurce"))));
        Assert.assertEquals(URI.create("http://www.example.org/rosource/rosource.rdf"), FolderResourceMap
                .generateResourceMapUri(new Folder(userProfile, researchObject, URI
                        .create("http://www.example.org/rosource/"))));
    }


    @Test
    public void testExtractFolderEntries()
            throws BadRequestException {
        Folder folder = folderBuilder.init(folderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject,
            folderResourceMapUri.resolve("folder"));
        FolderResourceMap folderResourceMap = builder.buildFolderResourceMap(folderResourceMapUri, folder);
        folderResourceMap.save();
        Assert.assertEquals(4, folderResourceMap.extractFolderEntries().size());
    }


    @Test
    public void testExtractResearchObject()
            throws BadRequestException {
        Folder folder = folderBuilder.init(folderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject,
            folderResourceMapUri.resolve("folder"));
        FolderResourceMap folderResourceMap = builder.buildFolderResourceMap(folderResourceMapUri, folder);
        folderResourceMap.save();
        Assert.assertEquals(researchObject, folderResourceMap.extractResearchObject());
    }


    @Test
    public void testSave()
            throws BadRequestException {
        Folder folder = folderBuilder.init(folderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject,
            folderResourceMapUri.resolve("folder"));
        FolderResourceMap folderResourceMap = builder.buildFolderResourceMap(folderResourceMapUri, folder);
        folderResourceMap.save();
        Model model = ModelFactory.createDefaultModel();
        model.read(folderResourceMap.getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(folderResourceMap.getUri().toString());
        Assert.assertTrue(r.hasProperty(ORE.describes, model.getResource(folder.getUri().toString())));
        Assert.assertTrue(r.hasProperty(RDF.type, ORE.ResourceMap));
    }
}
