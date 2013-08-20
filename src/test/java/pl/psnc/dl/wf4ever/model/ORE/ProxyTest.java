package pl.psnc.dl.wf4ever.model.ORE;

import java.io.InputStream;
import java.net.URI;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.AbstractUnitTest;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.vocabulary.ORE;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class ProxyTest extends AbstractUnitTest {

    private URI proxyUri;


    @Override
    @Before
    public void setUp()
            throws Exception {
        super.setUp();
        proxyUri = researchObject.getUri().resolve("proxy");
    }


    @Test
    public void testConstructor() {
        Proxy proxy = new Proxy(userProfile, dataset, true, proxyUri);
        Assert.assertEquals(proxyUri, proxy.getUri());
        Assert.assertEquals(proxyUri, proxy.getUri());
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
        model.read(researchObject.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
        Assert.assertFalse(model.containsResource(model.getResource(proxyUri.toString())));
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


    @Test(expected = NullPointerException.class)
    public void testAssembleNullContent()
            throws BadRequestException {
        Proxy.assemble(researchObject, null);
    }


    @Test(expected = NullPointerException.class)
    public void testAssembleNullContentNullRO()
            throws BadRequestException {
        Proxy.assemble(null, null);
    }


    @Test(expected = NullPointerException.class)
    public void testAssembleNullRO()
            throws BadRequestException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("model/ore/proxy/proxy.rdf");
        URI proxyUri = Proxy.assemble(null, is);
    }


    @Test
    public void testAssembleNotSavedRO()
            throws BadRequestException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("model/ore/proxy/proxy.rdf");
        URI proxyUri = Proxy.assemble(builder.buildResearchObject(researchObject.getUri().resolve("../not-saved-ro")),
            is);
    }
}
