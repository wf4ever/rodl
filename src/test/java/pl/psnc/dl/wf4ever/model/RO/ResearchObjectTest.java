package pl.psnc.dl.wf4ever.model.RO;

import java.io.InputStream;
import java.net.URI;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.vocabulary.ORE;
import pl.psnc.dl.wf4ever.vocabulary.ROEVO;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class ResearchObjectTest extends BaseTest {

    private URI researchObjectUri;


    @Override
    @Before
    public void setUp() {
        super.setUp();
        researchObjectUri = researchObject.getUri().resolve("new-research-object-unit-test/");
        clearDLFileSystem();
    }


    @Test
    public void testConstructor() {
        ResearchObject ro = new ResearchObject(userProfile, researchObjectUri);
        ro = new ResearchObject(userProfile, dataset, true, researchObjectUri);
    }


    @Test
    public void testCreate() {
        ResearchObject ro = ResearchObject.create(builder, researchObjectUri);
        Assert.assertNotNull(ro.getManifest());
        Assert.assertNotNull(ro.getEvoInfo());

        Model model = ModelFactory.createDefaultModel();

        model.read(ro.getEvoInfo().getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(ro.getUri().toString());
        Assert.assertTrue(r.hasProperty(RDF.type, ROEVO.LiveRO));

        model.read(ro.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
        r = model.getResource(ro.getUri().toString());
        Assert.assertTrue(r.hasProperty(ORE.isDescribedBy, model.getResource(ro.getManifest().getUri().toString())));
    }


    @Test(expected = ConflictException.class)
    public void testCreateDuplication() {
        ResearchObject ro = ResearchObject.create(builder, researchObjectUri);
        ro = ResearchObject.create(builder, researchObjectUri);
    }


    @Test
    public void testCreateEvoInfo() {
        //FIXME is there any way to test it?
        /*
            ResearchObject ro = builder.buildResearchObject(researchObjectUri);
            Assert.assertNull(ro.getEvoInfo());
        */
        /*
         * 
            researchObject ro = ResearchObject.create(builder, URI.create("http://example.org/unit-test-ro/"));
            EvoInfo evo1 = ro.getEvoInfo();
            ro.createEvoInfo();
            Assert.assertFalse(evo1.equals(ro.getEvoInfo()));
         */
    }


    @Test
    public void testGetEvoInfo() {
        ResearchObject ro = ResearchObject.create(builder, researchObjectUri);
        Assert.assertNotNull(ro.getEvoInfo());
    }


    @Test
    public void testGetLiveEvoInfo() {
        ResearchObject ro = ResearchObject.create(builder, researchObjectUri);
        Assert.assertNotNull(ro.getLiveEvoInfo());
    }


    @Test
    public void testGetManifest() {
        ResearchObject ro = ResearchObject.create(builder, researchObjectUri);
        Assert.assertNotNull(ro.getManifest());
    }


    @Test
    public void testGet() {
        ResearchObject ro = ResearchObject.create(builder, researchObjectUri);
        Assert.assertEquals(ro, ResearchObject.get(builder, ro.getUri()));
    }


    @Test
    public void testSave() {
        ResearchObject ro = builder.buildResearchObject(researchObjectUri);
        ro.save();

        Model model = ModelFactory.createDefaultModel();

        model.read(ro.getEvoInfo().getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(ro.getUri().toString());
        Assert.assertTrue(r.hasProperty(RDF.type, ROEVO.LiveRO));

        model.read(ro.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
        r = model.getResource(ro.getUri().toString());
        Assert.assertTrue(r.hasProperty(ORE.isDescribedBy, model.getResource(ro.getManifest().getUri().toString())));

        //TODO
        //check the serialization
    }


    @Test
    public void testDelete() {
        ResearchObject ro = ResearchObject.create(builder, researchObjectUri);
        ro.delete();
        Assert.assertEquals(ro, ResearchObject.get(builder, researchObjectUri));
    }


    @Test
    public void testGetImmutableResearchObjects() {
        //TODO implement test!
    }


    @Test
    public void testAggregate()
            throws BadRequestException {
        ResearchObject ro = ResearchObject.create(builder, researchObjectUri);
        InputStream is = getClass().getClassLoader().getResourceAsStream("model/ro/research_object/resource.txt");
        pl.psnc.dl.wf4ever.model.RO.Resource r = ro.aggregate("resource.txt", is, "text/plain");
        Assert.assertNotNull(ro.getAggregatedResources().get(r.getUri()));
        Assert.assertEquals(ro, r.getResearchObject());
        Model model = ModelFactory.createDefaultModel();
        model.read(ro.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource jenaResource = model.getResource(ro.getUri().toString());
        Assert.assertTrue(jenaResource.hasProperty(ORE.aggregates, model.getResource(r.getUri().toString())));
    }


    @Test
    public void testAggregateExternal() {
        ResearchObject ro = ResearchObject.create(builder, researchObjectUri);
        ro.aggregate(researchObject.getUri());
        Assert.assertNotNull(ro.getAggregatedResources().get(researchObject.getUri()));
    }

}
