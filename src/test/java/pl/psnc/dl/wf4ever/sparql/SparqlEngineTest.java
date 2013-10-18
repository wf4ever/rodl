package pl.psnc.dl.wf4ever.sparql;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.AbstractUnitTest;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.DCTerms;

/**
 * Test the class that performs SPARQL queries.
 * 
 * @author piotrekhol
 * 
 */
public class SparqlEngineTest extends AbstractUnitTest {

    /** The instance under test. */
    private SparqlEngine engine;

    /** A describe query. */
    private static String describeQuery;

    /** A construct query. */
    private static String constructQuery;

    /** A select query. */
    private static String selectQuery;

    /** An ask query that should return "true". */
    private static String askTrueQuery;

    /** An ask query that should return "false". */
    private static String askFalseQuery;


    /**
     * Load the queries.
     * 
     * @throws IOException
     *             when there is a problem with opening the resources with queries
     */
    @BeforeClass
    public static void setUpBeforeClass()
            throws IOException {
        describeQuery = String.format("DESCRIBE <%s>", RESOURCE1);
        InputStream is = SparqlEngineTest.class.getClassLoader().getResourceAsStream(
            "sparql/direct-annotations-construct.sparql");
        constructQuery = IOUtils.toString(is, "UTF-8");
        is = SparqlEngineTest.class.getClassLoader().getResourceAsStream("sparql/direct-annotations-select.sparql");
        selectQuery = IOUtils.toString(is, "UTF-8");
        is = SparqlEngineTest.class.getClassLoader().getResourceAsStream("sparql/direct-annotations-ask-true.sparql");
        askTrueQuery = IOUtils.toString(is, "UTF-8");
        is = SparqlEngineTest.class.getClassLoader().getResourceAsStream("sparql/direct-annotations-ask-false.sparql");
        askFalseQuery = IOUtils.toString(is, "UTF-8");

    }


    /**
     * Init the instance under test.
     * 
     * @throws Exception
     */
    @Before
    public void setUp()
            throws Exception {
        super.setUp();
        engine = new SparqlEngine(builder);
    }


    /**
     * Test executing a describe query.
     */
    @Test
    public final void testExecuteDescribeSparql() {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
        QueryResult res = engine.executeSparql(describeQuery, RDFFormat.RDFXML);
        model.read(res.getInputStream(), null, "RDF/XML");
        Individual resource = model.getIndividual(RESOURCE1);
        Assert.assertNotNull("Resource cannot be null", resource);
        Assert.assertTrue(String.format("Resource %s must be a ro:Resource", RESOURCE1),
            resource.hasRDFType(RO.NAMESPACE + "Resource"));
    }


    /**
     * Test executing a construct query.
     */
    @Test
    public final void testExecuteConstructSparql() {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
        model.read(engine.executeSparql(constructQuery, RDFFormat.RDFXML).getInputStream(), null, "RDF/XML");
        Assert.assertTrue("Construct contains triple 1",
            model.contains(model.createResource(RESOURCE1), DCTerms.title, "A test"));
        Assert.assertTrue("Construct contains triple 2",
            model.contains(model.createResource(RESOURCE1), DCTerms.license, "GPL"));
    }


    /**
     * Test executing a select query that returns an XML.
     * 
     * @throws IOException
     *             when converting the result to a string
     */
    @Test
    public final void testExecuteSelectXmlSparql()
            throws IOException {
        String xml = IOUtils.toString(engine.executeSparql(selectQuery, SparqlEngine.SPARQL_XML).getInputStream(),
            "UTF-8");
        // FIXME make more in-depth XML validation
        Assert.assertTrue("XML looks correct", xml.contains("Marco Roos"));
    }


    /**
     * Test executing a select query that returns a JSON.
     * 
     * @throws IOException
     *             when converting the result to a string
     */
    @Test
    public final void testExecuteSelectJsonSparql()
            throws IOException {
        String json = IOUtils.toString(engine.executeSparql(selectQuery, SparqlEngine.SPARQL_JSON).getInputStream(),
            "UTF-8");
        // FIXME make more in-depth JSON validation
        Assert.assertTrue("JSON looks correct", json.contains("Marco Roos"));
    }


    /**
     * Test executing an ask query that returns false.
     * 
     * @throws IOException
     *             when converting the result to a string
     */
    @Test
    public final void testExecuteAskFalseSparql()
            throws IOException {
        String xml = IOUtils.toString(engine.executeSparql(askFalseQuery, SparqlEngine.SPARQL_XML).getInputStream(),
            "UTF-8");
        Assert.assertTrue("XML looks correct", xml.contains("false"));
    }


    /**
     * Test executing an ask query that returns true.
     * 
     * @throws IOException
     *             when converting the result to a string
     */
    @Test
    public final void testExecuteAskTrueSparql()
            throws IOException {
        String xml = IOUtils.toString(engine.executeSparql(askTrueQuery, SparqlEngine.SPARQL_XML).getInputStream(),
            "UTF-8");
        Assert.assertTrue("XML looks correct", xml.contains("true"));
    }


    /**
     * Test that default response formats are correct even if an unexpected format is requested.
     */
    @Test
    public void testDefaultFormat() {
        RDFFormat jpeg = new RDFFormat("JPEG", "image/jpeg", Charset.forName("UTF-8"), "jpeg", false, false);
        QueryResult res = engine.executeSparql(describeQuery, jpeg);
        Assert.assertEquals("RDF/XML is the default format", RDFFormat.RDFXML, res.getFormat());
        res = engine.executeSparql(constructQuery, jpeg);
        Assert.assertEquals("RDF/XML is the default format", RDFFormat.RDFXML, res.getFormat());
        res = engine.executeSparql(selectQuery, jpeg);
        Assert.assertEquals("SPARQL XML is the default format", SparqlEngine.SPARQL_XML, res.getFormat());
        res = engine.executeSparql(askTrueQuery, jpeg);
        Assert.assertEquals("SPARQL XML is the default format", SparqlEngine.SPARQL_XML, res.getFormat());
    }

}
