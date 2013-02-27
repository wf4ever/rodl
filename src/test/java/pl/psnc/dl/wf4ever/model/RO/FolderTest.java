package pl.psnc.dl.wf4ever.model.RO;

import java.io.InputStream;
import java.net.URI;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.model.SnapshotBuilder;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class FolderTest extends BaseTest {

    private String folderName = "folder";
    private FolderBuilder folderBuilder;
    private URI folderUri;


    @Override
    @Before
    public void setUp() {
        super.setUp();
        folderUri = researchObject.getUri().resolve(folderName);
        folderBuilder = new FolderBuilder();
    }


    @Test
    public void testConstructor() {
        Folder folder = new Folder(userProfile, researchObject, folderUri);
        Assert.assertEquals(researchObject, folder.getResearchObject());
        Assert.assertEquals(folderUri, folder.getUri());
        folder = new Folder(userProfile, dataset, true, researchObject, folderUri);
        Assert.assertEquals(researchObject, folder.getResearchObject());
        Assert.assertEquals(folderUri, folder.getUri());
    }


    @Test
    public void testGetFolderEntries()
            throws BadRequestException {
        Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
        //WHY??
        Assert.assertEquals(4, folder.getFolderEntries().size());
    }


    @Test
    public void testGetAggregatedResources()
            throws BadRequestException {
        Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
        Assert.assertEquals(4, folder.getFolderEntries().size());
    }


    @Test
    public void testGetResourceMap()
            throws BadRequestException {
        Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
        Assert.assertNotNull(folder.getResourceMap());
        //TODO something else?
    }


    @Test
    public void testGet()
            throws BadRequestException {
        Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
        Folder.get(builder, folder.getUri());
        folder.equals(Folder.get(builder, folder.getUri()));
    }


    @Test
    public void testSave() {
        Folder folder = builder.buildFolder(researchObject, folderUri, userProfile, DateTime.now());
        folder.save();
        Model model = ModelFactory.createDefaultModel();
        model.read(researchObject.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
        Assert.assertNotNull(model.getResource(folder.getUri().toString()));
        Assert.assertNotNull(researchObject.getFolders().get(folder.getUri()));
    }


    @Test
    public void testDelete() {
        Folder folder = builder.buildFolder(researchObject, folderUri, userProfile, DateTime.now());
        folder.save();
        Model model = ModelFactory.createDefaultModel();
        model.read(researchObject.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
        Assert.assertNotNull(model.getResource(folder.getUri().toString()));

        folder.delete();
        model = ModelFactory.createDefaultModel();
        model.read(researchObject.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
        Assert.assertNull(model.getResource(folder.getUri().toString()));
    }


    @Test
    public void testCreate()
            throws BadRequestException {
        Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
        Assert.assertNotNull(folder);
    }


    @Test
    public void testCreateEmptyFolder()
            throws BadRequestException {
        Folder folder = folderBuilder.init("model/ro/folder/empty_folder.rdf", builder, researchObject, folderUri);
        Assert.assertNotNull(folder);
    }


    @Test
    public void testCreateFolderNoRDFContent()
            throws BadRequestException {
        Folder folder = folderBuilder.init("model/ro/folder/empty.rdf", builder, researchObject, folderUri);
    }


    public void testCreateFolderRDFNoFolder()
            throws BadRequestException {
        Folder folder = folderBuilder.init("model/ro/folder/no_folder.rdf", builder, researchObject, folderUri);
        Assert.assertNull(folder);
    }


    @Test(expected = BadRequestException.class)
    public void testCreateTwoFolders()
            throws BadRequestException {
        Folder folder = folderBuilder.init("model/ro/folder/two_folders.rdf", builder, researchObject, folderUri);
        Assert.assertNull(folder);
    }


    @Test(expected = ConflictException.class)
    public void testCreateDuplication()
            throws BadRequestException {
        Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
        folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
        Assert.assertNull(folder);
    }


    @Test
    public void testCreateFolderNullInputStream()
            throws BadRequestException {
        folderBuilder.createROAggregated(builder, researchObject, folderUri);
        Folder folder = Folder.create(builder, researchObject, folderUri, null);

    }


    @Test
    public void testCopy()
            throws BadRequestException {
        Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
        Folder folderCopy = folder.copy(builder, new SnapshotBuilder(), researchObject2);
        Assert.assertNotNull(folder.getCopyAuthor());
        Assert.assertNotNull(folder.getCopyDateTime());
        Boolean foldersNamesEqual = folderCopy.getUri().relativize(researchObject2.getUri())
                .equals(folder.getUri().relativize(researchObject.getUri()));
        Assert.assertTrue(foldersNamesEqual);
        for (FolderEntry entry : folderCopy.getFolderEntries().values()) {
            Assert.assertEquals(entry.getUri().relativize(researchObject2.getUri()), entry.getEntryName());
        }
        for (AggregatedResource resource : folderCopy.getAggregatedResources().values()) {
            Assert.assertEquals(resource.getName(), resource.getUri().relativize(researchObject2.getUri()));
        }
    }


    @Test
    public void testCreateFolderEntry()
            throws BadRequestException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(FolderBuilder.DEFAULT_FOLDER_PATH);
        researchObject.aggregate("new-resource", is, "text/plain");
        Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
        is = getClass().getClassLoader().getResourceAsStream("model/ro/folder/folder_entry.rdf");
        FolderEntry fe = folder.createFolderEntry(is);
        Assert.assertEquals(folder.getFolderEntries().get(fe.getUri()), fe);
    }


    @Test(expected = ConflictException.class)
    public void testCreateDuplicatedFolderEntry()
            throws BadRequestException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(FolderBuilder.DEFAULT_FOLDER_PATH);
        researchObject.aggregate("new-resource", is, "text/plain");
        Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
        is = getClass().getClassLoader().getResourceAsStream("model/ro/folder/duplicated_folder_entry.rdf");
        FolderEntry fe = folder.createFolderEntry(is);
        Assert.assertEquals(folder.getFolderEntries().get(fe.getUri()), fe);
    }


    @Test
    public void testAddFolderEntry()
            throws BadRequestException {
        Folder f = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
        FolderEntry fe = builder.buildFolderEntry(folderUri.resolve("fe"),
            f.getAggregatedResources().get(f.getAggregatedResources().values().iterator().next()), f, "fe");
        f.addFolderEntry(fe);
        Assert.assertEquals(fe, f.getFolderEntries().get(fe.getUri()));
    }


    @Test
    public void testAddFolderEntryAsNull()
            throws BadRequestException {
        Folder f = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject, folderUri);
        f.addFolderEntry(null);
    }


    @Test
    public void testUpdate()
            throws BadRequestException {
        Folder f = folderBuilder.init("model/ro/folder/empty_folder.rdf", builder, researchObject, folderUri);
        Assert.assertEquals(0, f.getFolderEntries().size());
        InputStream is = getClass().getClassLoader().getResourceAsStream(FolderBuilder.DEFAULT_FOLDER_PATH);
        f.update(is, "RDF/XML");
        Assert.assertEquals(4, f.getFolderEntries().size());
    }

}