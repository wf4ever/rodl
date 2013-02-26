package pl.psnc.dl.wf4ever.model.ORE;

import java.io.InputStream;
import java.net.URI;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.vocabulary.ORE;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

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
        proxy.save();
        Model model = ModelFactory.createDefaultModel();
        model.read(researchObject.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
        Resource r = model.getResource(proxyUri.toString());
        Assert.assertEquals(r.getURI(), proxyUri.toString());
        Assert.assertEquals(aggregatedResource.getUri().toString(), r.getProperty(ORE.proxyFor).getObject()
                .asResource().getURI());
        Assert.assertEquals(researchObject.getUri().toString(), r.getProperty(ORE.proxyIn).getObject().asResource()
                .getURI());
        //TODO WHY??
        //WHY??
        ResearchObject ro = ResearchObject.get(builder, researchObject.getUri());
        Assert.assertNotNull(ro.getProxies().get(proxy.getUri()));
    }


    @Test
    public void testDelete() {
        URI aggregatedResourceUri = researchObject.getUri().resolve("aggregated-resource");
        AggregatedResource aggregatedResource = builder.buildAggregatedResource(aggregatedResourceUri, researchObject,
            userProfile, DateTime.now());
        Proxy proxy = builder.buildProxy(proxyUri, aggregatedResource, researchObject);
        proxy.save();
        proxy.delete();
        Model model = ModelFactory.createDefaultModel();
        //TODO WHY??
        //WHY??
        model.read(researchObject.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
        Assert.assertNull(model.getResource(proxyUri.toString()));
    }


    @Test
    public void testAssemble()
            throws BadRequestException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("model/ore/proxy/proxy.rdf");
        URI createdUri = Proxy.assemble(researchObject, is);
        Assert.assertNotNull(proxyUri);
        Model model = ModelFactory.createDefaultModel();
        model.read(researchObject.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
        Assert.assertNotNull(model.getResource(createdUri.toString()));

    }


    @Test(expected = BadRequestException.class)
    public void testAssembleNullContent()
            throws BadRequestException {
        URI proxyUri = Proxy.assemble(researchObject, null);
    }


    @Test
    public void testAssembleNullContentNullRO()
            throws BadRequestException {
        //TODO Null Pointer Excpetion or BadRequest?
        URI proxyUri = Proxy.assemble(null, null);
    }


    @Test
    public void testAssembleNullRO()
            throws BadRequestException {
        //TODO Null Pointer Excpetion or BadRequest?
        InputStream is = getClass().getClassLoader().getResourceAsStream("model/ore/proxy/proxy.rdf");
        URI proxyUri = Proxy.assemble(null, is);
    }


    @Test
    public void testAssembleNotSavedRO()
            throws BadRequestException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("model/ore/proxy/proxy.rdf");
        URI proxyUri = Proxy.assemble(new ResearchObject(userProfile, researchObject.getUri()
                .resolve("../not-saved-ro")), is);
    }
}
