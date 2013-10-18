package pl.psnc.dl.wf4ever.model.RDF;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.AbstractUnitTest;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.vocabulary.ORE;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.DCTerms;

/**
 * Test Class for RDF.Thing model.
 * 
 * @author pejot
 * 
 */
public class ThingTest extends AbstractUnitTest {

    /**
     * Test that the resources under control of RODL are properly detected.
     */
    //FIXME this method is buggy, it shouldn't use URIs
    @Test
    public void testIsSpecial() {
        Thing ordinaryThing1 = builder.buildThing(URI.create("http:///www.example.com/ROS/manifest/thing/"));
        Thing ordinaryThing2 = builder.buildThing(URI.create("http:///www.example.com/ROs/manifest/"));
        Thing ordinaryThing3 = builder.buildThing(URI.create("http:///www.example.com/ROs/manifest.rdf/thing/"));
        Thing ordinaryThing4 = builder.buildThing(URI.create("http:///www.example.com/ROs/oridnary_resource"));
        Thing specialThing1 = builder.buildThing(URI.create("http:///www.example.com/ROs/manifest.rdf/"));
        Thing specialThing2 = builder.buildThing(URI.create("http:///www.example.com/ROs/evo_info.ttl/"));
        Thing specialThing3 = builder.buildThing(URI.create("http:///www.example.com/ROs/manifest.rdf"));
        Thing specialThing4 = builder.buildThing(URI.create("http:///www.example.com/ROs/evo_info.ttl"));
        Thing specialThing5 = builder.buildThing(URI.create("manifest.rdf"));
        Thing specialThing6 = builder.buildThing(URI.create("evo_info.ttl"));
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


    @Test
    public void testConstructor() {
        URI thingUri = URI.create("http://www.example.org/thing");
        Thing thing = new Thing(userProfile, dataset, true, thingUri);
        Assert.assertEquals(userProfile, thing.getUser());
        Assert.assertEquals(thingUri, thing.getUri());
    }


    @Test
    public void testGetUri() {
        //TODO Discuss the last case
        URI thingUri = URI.create("http://www.example.org/thing");
        URI thingUri2 = URI.create("http://www.example.org/thing/");
        Thing thing = builder.buildThing(thingUri);
        Thing thing2 = builder.buildThing(thingUri2);
        Assert.assertEquals(thing.getUri(), thingUri);
        Assert.assertEquals(thing2.getUri(), thingUri2);
        Assert.assertEquals(thing.getUri(RDFFormat.TURTLE),
            URI.create("http://www.example.org/thing.ttl?original=thing"));

        Assert.assertEquals(thing2.getUri(RDFFormat.RDFXML), URI.create("http://www.example.org/thing/.rdf?original="));

        Assert.assertEquals(thing.getUri(),
            thing.getUri(RDFFormat.TURTLE).resolve(thing.getUri(RDFFormat.TURTLE).getRawQuery().split("=")[1]));

        /*
         //java.lang.ArrayIndexOutOfBoundsException: 1
            Assert.assertEquals(thing2.getUri(),
            thing.getUri(RDFFormat.TURTLE).resolve(thing2.getUri(RDFFormat.TURTLE).getRawQuery().split("=")[1]));
         */
    }


    @Test
    public void testGetName() {
        URI thingUri = URI.create("http://www.example.org/thing");
        URI thingUri2 = URI.create("http://www.example.org/thing/");
        Thing thing = builder.buildThing(thingUri);
        Thing thing2 = builder.buildThing(thingUri2);
        Assert.assertEquals(thing.getName(), "thing");
        Assert.assertEquals(thing2.getName(), "thing");
    }


    @Test
    public void testIsNamedGraph() {
        Assert.assertFalse(researchObject2.isNamedGraph());
        Assert.assertTrue(researchObject.getManifest().isNamedGraph());
        ResearchObject noSavedRO = builder.buildResearchObject(URI.create("http://www.example.com/no-saved-ro/"));
        Assert.assertFalse(noSavedRO.isNamedGraph());
    }


    @Test
    public void testExtractCreator() {
        DateTime dt = researchObject.getManifest().extractCreated(researchObject);
        Assert.assertNotNull(dt);
    }


    @Test
    public void testSave() {
        URI thingUri = URI.create("http://www.example.org/thing");
        Thing thing = builder.buildThing(thingUri);
        thing.save();
        thing.save();
    }


    @Test
    public void testDelete() {
        researchObject.getManifest().serialize();
        Assert.assertTrue(dataset.containsNamedModel(researchObject.getManifest().getUri().toString()));
        ((Thing) (researchObject.getManifest())).delete();
        Assert.assertFalse(dataset.containsNamedModel(researchObject.getManifest().getUri().toString()));
    }


    @Test
    public void testExtractCreated() {
        UserMetadata um = researchObject.getManifest().extractCreator(researchObject);
        Assert.assertNotNull(um);
    }


    @Test
    public void testSerialize() {
        //TODO LEAVE IT ?
    }


    public void testTransactions() {
        //TODO HOW ?!

    }


    @Test
    public void testEquals() {
        Thing first = builder.buildThing(URI.create("http://example.org/example-thing"));
        Thing second = builder.buildThing(URI.create("http://example.org/example-thing"));
        Assert.assertTrue(first.equals(second));
    }


    @Test
    public void testGetGraphAsInputStream() {
        InputStream is = ((Thing) (researchObject.getManifest())).getGraphAsInputStream(RDFFormat.RDFXML);
        Assert.assertNotNull(is);
    }


    @Test
    public void testGetGraphAsInputStreamFromEmptyObject() {
        InputStream is = builder.buildThing(researchObject.getUri().resolve("a-fake-uri")).getGraphAsInputStream(
            RDFFormat.RDFXML);
        Assert.assertNull(is);
    }


    @Test
    public void testGetGraphAsInputStreamFromEmptyObject2() {
        researchObject.getManifest().serialize();
        ((Thing) (researchObject.getManifest())).delete();
        InputStream is = ((Thing) (researchObject.getManifest())).getGraphAsInputStream(RDFFormat.RDFXML);
        Assert.assertNull(is);
    }


    /**
     * Test the manifest can be retrieved together with the annotation body.
     */
    @Test
    public void testGetGraphAsInputStreamWithNamedGraphs() {
        Thing thing = builder.buildThing(URI.create(MANIFEST));
        Dataset dataset = DatasetFactory.createMem();
        RDFDataMgr.read(dataset, thing.getGraphAsInputStream(RDFFormat.TRIG), Lang.TRIG);
        Set<String> graphNames = new HashSet<>();
        Iterator<String> it = dataset.listNames();
        while (it.hasNext()) {
            graphNames.add(it.next());
        }
        assertThat(graphNames, hasItem(MANIFEST));
        assertThat(graphNames, hasItem(ANNOTATION_BODY));
        Model m = dataset.getDefaultModel();
        Assert.assertTrue(
            "Contains a sample aggregation",
            dataset.getNamedModel(MANIFEST).contains(m.createResource(RESEARCH_OBJECT), ORE.aggregates,
                m.createResource(RESOURCE1)));
        Assert.assertTrue(
            "Contains a sample aggregation",
            dataset.getNamedModel(ANNOTATION_BODY).contains(m.createResource(RESOURCE1), DCTerms.license,
                m.createLiteral("GPL")));
    }


    /**
     * Test the annotation body can be retrieved with relative URIs.
     */
    @Test
    public void testGetGraphAsInputStreamWithRelativeURIs() {
        Thing thing = builder.buildThing(URI.create(ANNOTATION_BODY));
        String base = "app://base/1/";
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
        model.read(thing.getGraphAsInputStreamWithRelativeURIs(URI.create(RESEARCH_OBJECT), RDFFormat.RDFXML), base,
            "RDF/XML");
        verifyTriple(model, /* "../a_workflow.t2flow" */"app://base/a%20workflow.t2flow",
            URI.create("http://purl.org/dc/terms/title"), "A test");
        verifyTriple(model, /* "../a_workflow.t2flow" */"app://base/a%20workflow.t2flow",
            URI.create(DCTerms.license.getURI()), "GPL");
        verifyTriple(model, URI.create(RESOURCE2), URI.create(DCTerms.description.getURI()), "Something interesting");
        verifyTriple(model, /* "../a_workflow.t2flow#somePartOfIt" */"app://base/a%20workflow.t2flow#somePartOfIt",
            URI.create(DCTerms.description.getURI()), "The key part");
    }


    @Test
    public void testGetDescriptionFor() {
        Thing manifest = researchObject.getManifest();
        Multimap<URI, Object> result = ((Thing) manifest).getDescriptionFor(researchObject.getUri());
        /* verify description
        for (Map.Entry<URI, Object> entry : result.entries()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
        */

    }
}
