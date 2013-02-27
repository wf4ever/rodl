package pl.psnc.dl.wf4ever.model.ORE;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
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


    /**
     * Test updating a text file. Check that the new content got serialized.
     * 
     * @throws BadRequestException
     *             shouldn't happen
     * @throws IOException
     *             when there's problem with test data
     */
    @Test
    public void testUpdate()
            throws BadRequestException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("model/ore/aggregated_resource/hello.txt");
        AggregatedResource aggregatedResource = AggregatedResource.create(builder, researchObject,
            aggregatedResourceUri, is, "text/plain");
        is.close();
        is = getClass().getClassLoader().getResourceAsStream("model/ore/aggregated_resource/bye.txt");
        aggregatedResource.update(is, "text/plain");
        is.close();
        is = getClass().getClassLoader().getResourceAsStream("model/ore/aggregated_resource/bye.txt");
        String expected = IOUtils.toString(is);
        is.close();
        String result = IOUtils.toString(aggregatedResource.getSerialization());
        Assert.assertEquals(expected, result);
    }


    @Test
    public void testUpdateRDF()
            throws BadRequestException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("model/ore/aggregated_resource/ann1.rdf");
        AggregatedResource aggregatedResource = AggregatedResource.create(builder, researchObject,
            aggregatedResourceUri, is, "application/rdf+xml");
        is.close();
        aggregatedResource.saveGraphAndSerialize();
        is = getClass().getClassLoader().getResourceAsStream("model/ore/aggregated_resource/ann2.rdf");
        aggregatedResource.update(is, "application/rdf+xml");
        is.close();
        is = getClass().getClassLoader().getResourceAsStream("model/ore/aggregated_resource/ann2.rdf");
        Model expected = ModelFactory.createDefaultModel();
        expected.read(is, "");
        is.close();
        Model result = ModelFactory.createDefaultModel();
        result.read(aggregatedResource.getGraphAsInputStream(RDFFormat.RDFXML), "");
        Assert.assertTrue(expected.isIsomorphicWith(result));
    }


    @Test
    public void testUpdateReferences()
            throws BadRequestException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("model/ore/aggregated_resource/ann2.rdf");
        AggregatedResource aggregatedResource = AggregatedResource.create(builder, researchObject,
            aggregatedResourceUri, is, "application/rdf+xml");
        is.close();
        aggregatedResource.saveGraphAndSerialize();
        aggregatedResource.updateReferences(researchObject2);
        is = getClass().getClassLoader().getResourceAsStream("model/ore/aggregated_resource/ann1.rdf");
        Model expected = ModelFactory.createDefaultModel();
        expected.read(is, "");
        is.close();
        Model result = ModelFactory.createDefaultModel();
        result.read(aggregatedResource.getGraphAsInputStream(RDFFormat.RDFXML), "");
        expected.write(System.out, "TURTLE");
        result.write(System.out, "TURTLE");
        Assert.assertTrue(expected.isIsomorphicWith(result));
    }
}
