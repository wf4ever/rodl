package pl.psnc.dl.wf4ever.model.RO;

import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;

public class FolderEntryTest extends BaseTest {

    private URI folderEntryUri;
    private FolderBuilder folderBuilder;


    @Override
    @Before
    public void setUp() {
        super.setUp();
        folderEntryUri = researchObject.getUri().resolve("folder-entry-uri");
        folderBuilder = new FolderBuilder();
    }


    @Test
    public void testConstructor() {
        FolderEntry fe = new FolderEntry(userProfile, folderEntryUri);
        Assert.assertEquals(folderEntryUri, fe.getUri());

        fe = new FolderEntry(userProfile, dataset, true, folderEntryUri);
        Assert.assertEquals(folderEntryUri, fe.getUri());
    }


    @Test
    public void testGenerateEntryName() {
        //FIXMY
        //is not a replication?
    }


    @Test
    public void testSave()
            throws BadRequestException {
        Folder f = folderBuilder.init(folderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject,
            folderEntryUri.resolve("folder"));
        FolderEntry folderEntry = builder.buildFolderEntry(folderEntryUri,
            f.getAggregatedResources().get(f.getAggregatedResources().values().iterator().next()), f, "folder-entry");
        Assert.assertNull(f.getFolderEntries().get(folderEntry.getUri()));
        f.save();
        folderEntry.save();
        Assert.assertNotNull(Folder.get(builder, folderEntryUri.resolve("folder")).getFolderEntries()
                .get(folderEntry.getUri()));
    }


    @Test
    public void testSaveSecondApproachStillNotWorking()
            throws BadRequestException {
        Folder f = folderBuilder.init(folderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject,
            folderEntryUri.resolve("folder"));
        FolderEntry folderEntry = builder.buildFolderEntry(folderEntryUri,
            f.getAggregatedResources().get(f.getAggregatedResources().values().iterator().next()), f, "folder-entry");
        Assert.assertNull(f.getFolderEntries().get(folderEntry.getUri()));
        f.addFolderEntry(folderEntry);
        folderEntry.save();
        Assert.assertNotNull(Folder.get(builder, folderEntryUri.resolve("folder")).getFolderEntries()
                .get(folderEntry.getUri()));
    }


    @Test
    public void testDelete()
            throws BadRequestException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(FolderBuilder.DEFAULT_FOLDER_PATH);
        researchObject.aggregate("new-resource", is, "text/plain");
        Folder folder = folderBuilder.init(FolderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject,
            folderEntryUri.resolve("folder"));
        is = getClass().getClassLoader().getResourceAsStream("model/ro/folder/folder_entry.rdf");
        FolderEntry fe = folder.createFolderEntry(is);
        Assert.assertNotNull(folder.getFolderEntries().get(fe.getUri()));
        fe.delete();
        Assert.assertNull(folder.getFolderEntries().get(fe.getUri()));
    }


    @Test
    public void testUpdate()
            throws BadRequestException {
        Folder f = folderBuilder.init(folderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject,
            folderEntryUri.resolve("folder"));
        researchObject.aggregate(URI.create("http://example.org/external2"));
        researchObject.aggregate(URI.create("http://example.org/external3"));
        Iterator<AggregatedResource> iter = f.getAggregatedResources().values().iterator();
        AggregatedResource ag1 = iter.next();
        AggregatedResource ag2 = iter.next();
        FolderEntry folderEntry = builder.buildFolderEntry(folderEntryUri, ag1, f, "folder-entry");
        FolderEntry folderEntry2 = builder.buildFolderEntry(folderEntryUri.resolve("folder-entry-2"), ag2, f,
            "folder-entry");
        folderEntry.update(folderEntry2);
        Assert.assertEquals(folderEntry.getEntryName(), folderEntry2.getEntryName());
    }


    @Test
    public void testCopy()
            throws BadRequestException {
        Folder f = folderBuilder.init(folderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject,
            folderEntryUri.resolve("folder"));
        researchObject.aggregate(URI.create("http://example.org/external2"));
        researchObject.aggregate(URI.create("http://example.org/external3"));
        Iterator<AggregatedResource> iter = f.getAggregatedResources().values().iterator();
        AggregatedResource ag1 = iter.next();
        AggregatedResource ag2 = iter.next();
        FolderEntry folderEntry = builder.buildFolderEntry(folderEntryUri, ag1, f, "folder-entry");
        int beforeCopy = f.getFolderEntries().size();
        folderEntry.copy(builder, f);
        Assert.assertEquals(beforeCopy + 1, f.getFolderEntries().size());
        //TODO
        //figure out why it doesn't work
        //improve check also uri
    }


    @Test
    public void testAssembly()
            throws BadRequestException {
        Folder f = folderBuilder.init(folderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject,
            folderEntryUri.resolve("folder"));
        InputStream is = getClass().getClassLoader().getResourceAsStream(FolderBuilder.DEFAULT_FOLDER_PATH);
        researchObject.aggregate("new-resource", is, "text/plain");
        is = getClass().getClassLoader().getResourceAsStream("model/ro/folder_entry/folder_entry.rdf");
        FolderEntry folderEntry = FolderEntry.assemble(builder, f, is);
        Assert.assertNotNull(folderEntry);
        Assert.assertNotNull(folderEntry.getEntryName());
    }


    @Test
    public void testAssemblyWithNoResource()
            throws BadRequestException {
        Folder f = folderBuilder.init(folderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject,
            folderEntryUri.resolve("folder"));
        InputStream is = getClass().getClassLoader().getResourceAsStream(FolderBuilder.DEFAULT_FOLDER_PATH);
        is = getClass().getClassLoader().getResourceAsStream("model/ro/folder_entry/folder_entry.rdf");
        FolderEntry folderEntry = FolderEntry.assemble(builder, f, is);
        Assert.assertNotNull(folderEntry);
        Assert.assertNotNull(folderEntry.getEntryName());
        //TODO no resource no assebly? 
        assert false;
    }


    @Test(expected = BadRequestException.class)
    public void testAssemblyNoProxyFor()
            throws BadRequestException {
        Folder f = folderBuilder.init(folderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject,
            folderEntryUri.resolve("folder"));
        InputStream is = getClass().getClassLoader().getResourceAsStream(FolderBuilder.DEFAULT_FOLDER_PATH);
        researchObject.aggregate("new-resource", is, "text/plain");
        is = getClass().getClassLoader().getResourceAsStream("model/ro/folder_entry/no_proxy_for_folder_entry.rdf");
        FolderEntry folderEntry = FolderEntry.assemble(builder, f, is);
    }


    @Test(expected = BadRequestException.class)
    public void testAssemblyNoName()
            throws BadRequestException {
        Folder f = folderBuilder.init(folderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject,
            folderEntryUri.resolve("folder"));
        InputStream is = getClass().getClassLoader().getResourceAsStream(FolderBuilder.DEFAULT_FOLDER_PATH);
        researchObject.aggregate("new-resource", is, "text/plain");
        is = getClass().getClassLoader().getResourceAsStream("model/ro/folder_entry/no_name_folder_entry.rdf");
        FolderEntry folderEntry = FolderEntry.assemble(builder, f, is);
    }

}
