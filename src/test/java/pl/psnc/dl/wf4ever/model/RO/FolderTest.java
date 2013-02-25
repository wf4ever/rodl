package pl.psnc.dl.wf4ever.model.RO;

import java.io.InputStream;
import java.net.URI;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.BaseTest;

public class FolderTest extends BaseTest {

    private String folderName = "folder";
    private URI folderUri;


    @Override
    @Before
    public void setUp() {
        super.setUp();
        folderUri = researchObject.getUri().resolve(folderName);
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
        InputStream is = getClass().getClassLoader().getResourceAsStream("model/ro/folder/folder.rdf");

        researchObject.aggregate(URI.create("http://example.org"));
        researchObject.aggregate("ar1", is, "text/plain");
        researchObject.aggregate("ar2", is, "text/plain");
        researchObject.aggregate("ar3", is, "text/plain");

        Folder folder = Folder.create(builder, researchObject, folderUri, null);

        Assert.assertEquals(3, folder.getFolderEntries().size());
    }

}
