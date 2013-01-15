package pl.psnc.dl.wf4ever.model.RDF;

import java.net.URI;

import junit.framework.Assert;

import org.junit.Test;

import pl.psnc.dl.wf4ever.model.BaseTest;

/**
 * Test Class for RDF.Thing model.
 * 
 * @author pejot
 * 
 */
public class ThingTest extends BaseTest {

    @Override
    public void setUp() {
        super.setUp();
    }


    @Test
    public void testIsSpecial() {
        Thing ordinaryThing1 = new Thing(null, URI.create("http:///www.example.com/ROS/manifest/thing/"));
        Thing ordinaryThing2 = new Thing(null, URI.create("http:///www.example.com/ROs/manifest/"));
        Thing ordinaryThing3 = new Thing(null, URI.create("http:///www.example.com/ROs/manifest.rdf/thing/"));
        Thing ordinaryThing4 = new Thing(null, URI.create("http:///www.example.com/ROs/oridnary_resource"));
        Thing specialThing1 = new Thing(null, URI.create("http:///www.example.com/ROs/manifest.rdf/"));
        Thing specialThing2 = new Thing(null, URI.create("http:///www.example.com/ROs/evo_info.ttl/"));
        Thing specialThing3 = new Thing(null, URI.create("http:///www.example.com/ROs/manifest.rdf"));
        Thing specialThing4 = new Thing(null, URI.create("http:///www.example.com/ROs/evo_info.ttl"));
        Thing specialThing5 = new Thing(null, URI.create("manifest.rdf"));
        Thing specialThing6 = new Thing(null, URI.create("evo_info.ttl"));
        Assert.assertFalse(ordinaryThing1.isSpecialResource());
        Assert.assertFalse(ordinaryThing2.isSpecialResource());
        Assert.assertFalse(ordinaryThing3.isSpecialResource());
        Assert.assertFalse(ordinaryThing4.isSpecialResource());
        Assert.assertTrue(specialThing1.isSpecialResource());
        Assert.assertTrue(specialThing2.isSpecialResource());
        Assert.assertTrue(specialThing3.isSpecialResource());
        Assert.assertTrue(specialThing4.isSpecialResource());
        Assert.assertTrue(specialThing6.isSpecialResource());
        Assert.assertTrue(specialThing5.isSpecialResource());
    }
}
