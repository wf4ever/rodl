package pl.psnc.dl.wf4ever.model.ORE;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.model.EvoBuilder;
import pl.psnc.dl.wf4ever.model.SnapshotBuilder;

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
        InputStream is = getClass().getClassLoader().getResourceAsStream("model/ore/ann1.rdf");
        aggregatedResource.update(is, "RDFXML");
        is = getClass().getClassLoader().getResourceAsStream("model/ore/ann1.rdf");
        String content = IOUtils.toString(aggregatedResource.getSerialization());
        String content2 = IOUtils.toString(is);
        Assert.assertEquals(content2, content);
    }


    @Test
    public void testUpdateReferences()
            throws BadRequestException, IOException {
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(aggregatedResourceUri, researchObject,
            userProfile, DateTime.now());
        aggregatedResource.save();
        InputStream is = getClass().getClassLoader().getResourceAsStream("model/ore/ann1.rdf");
        aggregatedResource.update(is, "RDFXML");
        aggregatedResource.updateReferences(researchObject2);
        is = getClass().getClassLoader().getResourceAsStream("model/ore/ann1.rdf");
        String content = IOUtils.toString(aggregatedResource.getSerialization());
        String content2 = IOUtils.toString(is);
        //Whyu not ?
        Assert.assertFalse(content2.equals(content));
    }
}
