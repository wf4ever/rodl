package pl.psnc.dl.wf4ever.model.AO;

import java.net.URI;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.ORE.Proxy;

public class ProxyTest extends BaseTest {

    private URI proxyUri;


    @Override
    @Before
    public void setUp() {
        super.setUp();
        proxyUri = researchObject.getUri().resolve("proxy");
    }


    @Test
    public void testConstructor() {
        Proxy proxy = new Proxy(userProfile);
        Assert.assertEquals(userProfile, proxy.getCreator());
        proxy = new Proxy(userProfile, proxyUri);
        Assert.assertEquals(userProfile, proxy.getCreator());
        Assert.assertEquals(proxyUri, proxy.getUri());
        proxy = new Proxy(userProfile, dataset, true, proxyUri);
        Assert.assertEquals(proxyUri, proxy.getUri());
        Assert.assertEquals(proxyUri, proxy.getUri());
    }


    @Test
    public void testCreate() {
        URI aggregatedResourceUri = researchObject.getUri().resolve("aggregated-resource");
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(aggregatedResourceUri, researchObject,
            userProfile, DateTime.now());
        Proxy proxy = Proxy.create(builder, researchObject, aggregatedResource);
        Assert.assertEquals(aggregatedResource, proxy.getProxyFor());
        Assert.assertEquals(researchObject, proxy.getProxyIn());

    }


    @Test
    public void testSave() {
        URI aggregatedResourceUri = researchObject.getUri().resolve("aggregated-resource");
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(aggregatedResourceUri, researchObject,
            userProfile, DateTime.now());
        Proxy proxy = builder.buildProxy(proxyUri, aggregatedResource, researchObject);
        Assert.assertNull(researchObject.getProxies().get(proxyUri));
        proxy.save();
        //ResearchObject ro = researchObject.get(builder, researchObject.getUri());
        //Assert.assertEquals(proxy, ro.getProxies().get(proxyUri));
    }
}
