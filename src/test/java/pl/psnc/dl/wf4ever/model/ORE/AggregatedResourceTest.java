package pl.psnc.dl.wf4ever.model.ORE;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.model.EvoBuilder;
import pl.psnc.dl.wf4ever.model.SnapshotBuilder;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class AggregatedResourceTest extends BaseTest {

    private URI aggregatedResourceUri;


    @Override
    @Before
    public void setUp() {
        super.setUp();
        aggregatedResourceUri = researchObject.getUri().resolve("aggregated-resource");
    }


    @Test
    public void testConstructor() {
        AggregatedResource aggregatedResource = new AggregatedResource(userProfile, researchObject,
                aggregatedResourceUri);
        Assert.assertEquals(researchObject, aggregatedResource.getResearchObject());
        Assert.assertEquals(aggregatedResourceUri, aggregatedResource.getUri());

        aggregatedResource = new AggregatedResource(userProfile, dataset, true, researchObject, aggregatedResourceUri);
        Assert.assertEquals(researchObject, aggregatedResource.getResearchObject());
        Assert.assertEquals(aggregatedResourceUri, aggregatedResource.getUri());
    }


    @Test
    public void testCopy()
            throws BadRequestException {
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(aggregatedResourceUri, researchObject,
            userProfile, DateTime.now());
        EvoBuilder evoBuilder = new SnapshotBuilder();
        AggregatedResource copyAggregatedResource = aggregatedResource.copy(builder, evoBuilder, researchObject2);
        Assert.assertEquals(researchObject.getUri(), aggregatedResource.getUri().relativize(researchObject.getUri()));
        Assert.assertEquals(researchObject2.getUri(),
            copyAggregatedResource.getUri().relativize(researchObject2.getUri()));
        Assert.assertNotNull(copyAggregatedResource.getCopyDateTime());
        Assert.assertNotNull(copyAggregatedResource.getCopyAuthor());
        //TODO
        //getCopyOf -> clarify what it should do 
    }


    @Test(expected = NullPointerException.class)
    public void testCopyToNull()
            throws BadRequestException {
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(aggregatedResourceUri, researchObject,
            userProfile, DateTime.now());
        EvoBuilder evoBuilder = new SnapshotBuilder();
        AggregatedResource copyAggregatedResource = aggregatedResource.copy(builder, evoBuilder, null);
    }


    @Test
    public void testSave() {
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(aggregatedResourceUri, researchObject,
            userProfile, DateTime.now());
        aggregatedResource.save();
        Assert.assertNotNull(researchObject.getManifest().extractCreated(aggregatedResource));
        Assert.assertNotNull(researchObject.getManifest().extractCreator(aggregatedResource));
        Assert.assertNotNull(researchObject.getAggregatedResources().get(aggregatedResourceUri));
    }


    @Test
    public void testUpdate()
            throws BadRequestException, IOException {
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(aggregatedResourceUri, researchObject,
            userProfile, DateTime.now());
        aggregatedResource.save();
        InputStream is = getClass().getClassLoader().getResourceAsStream("model/ore/aggregated_resource/ann1.rdf");
        aggregatedResource.update(is, "RDF/XML");
        is = getClass().getClassLoader().getResourceAsStream("model/ore/aggregated_resource/ann1.rdf");
        Model model = ModelFactory.createDefaultModel();
        model.read(aggregatedResource.getGraphAsInputStream(RDFFormat.RDFXML), null);
        Model model2 = ModelFactory.createDefaultModel();
        model2.read(is, null);
        Assert.assertTrue(model.isIsomorphicWith(model2));
        //model is empty. why ?
        assert false;
    }


    @Test
    public void testUpdateReferences()
            throws BadRequestException, IOException {
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(aggregatedResourceUri, researchObject,
            userProfile, DateTime.now());
        aggregatedResource.save();
        InputStream is = getClass().getClassLoader().getResourceAsStream("model/ore/aggregated_resource/ann1.rdf");
        aggregatedResource.update(is, "RDF/XML");
        aggregatedResource.updateReferences(researchObject2);
        is = getClass().getClassLoader().getResourceAsStream("model/ore/aggregated_resource/ann1.rdf");
        Model model = ModelFactory.createDefaultModel();
        model.read(aggregatedResource.getGraphAsInputStream(RDFFormat.RDFXML), null);
        Model model2 = ModelFactory.createDefaultModel();
        model2.read(is, null);
        System.out.println("ro1:");
        System.out.println(researchObject.getUri());
        System.out.println("ro2:");
        System.out.println(researchObject2.getUri());
        System.out.println("ro1-model:");
        for (Statement s : model.listStatements().toList()) {
            String a = s.toString();
            System.out.println(a);
        }
        System.out.println("ro2-model:");
        for (Statement s : model2.listStatements().toList()) {
            String a = s.toString();
            System.out.println(a);
        }
        Assert.assertFalse(model.isIsomorphicWith(model2));
        //model is empty. why ?
        assert false;
    }
}
