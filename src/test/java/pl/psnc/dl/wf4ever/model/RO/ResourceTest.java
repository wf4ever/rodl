package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;

public class ResourceTest extends BaseTest {

    private URI resourceUri;


    @Override
    @Before
    public void setUp() {
        // TODO Auto-generated method stub
        super.setUp();
        resourceUri = researchObject.getUri().resolve("resource");
    }


    @Test
    public void testConstructor() {

        Resource resource = new Resource(userProfile, dataset, true, researchObject, resourceUri);
    }


    @Test
    public void testCreate() {
        Resource resource = Resource.create(builder, researchObject, resourceUri);
        Assert.assertTrue(researchObject.getResources().containsKey(resourceUri));
    }


    @Test
    public void testSave() {
        Resource resource = Resource.create(builder, researchObject, resourceUri);
        resource.save();
        Model model = ModelFactory.createDefaultModel();
        model.read(researchObject.getGraphAsInputStream(RDFFormat.RDFXML), null);
        com.hp.hpl.jena.rdf.model.Resource r = model.getResource(resourceUri.toString());
        Assert.assertTrue(r.hasProperty(RDF.type, RO.Resource));
    }


    @Test
    public void testDelete() {
        Resource resource = Resource.create(builder, researchObject, resourceUri);
        resource.delete();
        Assert.assertFalse(researchObject.getResources().containsKey(resourceUri));
    }


    @Test
    public void testSaveGraphAndSerialize()
            throws BadRequestException {
        //TODO test serialization
        Resource resource = builder.buildResource(resourceUri, researchObject, userProfile, DateTime.now());
        resource.save();
        resource.saveGraphAndSerialize();
        Model model = ModelFactory.createDefaultModel();
        model.read(researchObject.getGraphAsInputStream(RDFFormat.RDFXML), null);
        com.hp.hpl.jena.rdf.model.Resource r = model.getResource(resourceUri.toString());
        Assert.assertTrue(r.hasProperty(RDF.type, RO.Resource));
    }
}
