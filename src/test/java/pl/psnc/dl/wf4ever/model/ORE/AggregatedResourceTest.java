package pl.psnc.dl.wf4ever.model.ORE;

import java.net.URI;

import junit.framework.Assert;

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
            userProfile, DateTime.now().minusDays(3));
        EvoBuilder evoBuilder = new SnapshotBuilder();
        AggregatedResource copyAggregatedResource = aggregatedResource.copy(builder, evoBuilder, researchObject2);
        Assert.assertEquals(researchObject.getUri(), aggregatedResource.getUri().relativize(researchObject.getUri()));
        Assert.assertEquals(researchObject2.getUri(),
            copyAggregatedResource.getUri().relativize(researchObject2.getUri()));

    }
}
