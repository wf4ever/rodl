package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.vocabulary.ORE;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
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
    public void testSave()
            throws BadRequestException {
        Folder folder = folderBuilder.init(folderBuilder.DEFAULT_FOLDER_PATH, builder, researchObject,
            folderResourceMapUri.resolve("folder"));
        FolderResourceMap folderResourceMap = builder.buildFolderResourceMap(folderResourceMapUri, folder);
        folderResourceMap.save();
        Model model = ModelFactory.createDefaultModel();
        model.read(folderResourceMap.getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(folderResourceMap.getUri().toString());
        System.out.println(r.getProperty(ORE.describes));
        Assert.assertTrue(r.hasProperty(ORE.describes, model.getResource(folder.getUri().toString())));
        Assert.assertTrue(r.hasProperty(RDF.type, ORE.ResourceMap));
    }
}
