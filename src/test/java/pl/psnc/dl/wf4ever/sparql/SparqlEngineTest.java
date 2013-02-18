package pl.psnc.dl.wf4ever.sparql;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.sms.QueryResult;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DCTerms;

public class SparqlEngineTest {

    private SparqlEngine engine;
    private static String describeQuery;
    private static String constructQuery;
    private static String selectQuery;
    private static String askTrueQuery;
    private static String askFalseQuery;
    private Builder builder;

    private static final String MANIFEST = "http://example.org/mess-ro/.ro/manifest.ttl";

    private static final String ANNOTATION_BODY = "http://example.org/mess-ro/.ro/annotationBody.ttl";

    private static final String RESOURCE1 = "http://example.org/mess-ro/a%20workflow.t2flow";


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


    @Before
    public void setUp()
            throws Exception {
        Dataset dataset = TDBFactory.createDataset();
        Model model;
        model = FileManager.get().loadModel(MANIFEST, MANIFEST, "TURTLE");
        dataset.addNamedModel(MANIFEST, model);
        model.write(System.out, "TURTLE");
        model = FileManager.get().loadModel(ANNOTATION_BODY, ANNOTATION_BODY, "TURTLE");
        model.write(System.out, "TURTLE");
        dataset.addNamedModel(ANNOTATION_BODY, model);

        UserMetadata user = new UserMetadata("x", "x", Role.AUTHENTICATED, URI.create("http://x"));
        builder = new Builder(user, dataset, false);
        engine = new SparqlEngine(builder);
    }


    @After
    public void tearDown()
            throws Exception {
        builder.getDataset().close();
    }


    @Test
    public final void testExecuteDescribeSparql()
            throws IOException {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
        QueryResult res = engine.executeSparql(describeQuery, RDFFormat.RDFXML);
        model.read(res.getInputStream(), null, "RDF/XML");
        Individual resource = model.getIndividual(RESOURCE1);
        Assert.assertNotNull("Resource cannot be null", resource);
        Assert.assertTrue(String.format("Resource %s must be a ro:Resource", RESOURCE1),
            resource.hasRDFType(RO.NAMESPACE + "Resource"));
    }


    @Test
    public final void testExecuteConstructSparql()
            throws IOException {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
        model.read(engine.executeSparql(constructQuery, RDFFormat.RDFXML).getInputStream(), null, "RDF/XML");
        Assert.assertTrue("Construct contains triple 1",
            model.contains(model.createResource(RESOURCE1), DCTerms.title, "A test"));
        Assert.assertTrue("Construct contains triple 2",
            model.contains(model.createResource(RESOURCE1), DCTerms.license, "GPL"));
    }


    @Test
    public final void testExecuteSelectXmlSparql()
            throws IOException {
        String xml = IOUtils.toString(engine.executeSparql(selectQuery, SparqlEngine.SPARQL_XML).getInputStream(),
            "UTF-8");
        // FIXME make more in-depth XML validation
        Assert.assertTrue("XML looks correct", xml.contains("Marco Roos"));
    }


    @Test
    public final void testExecuteSelectJsonSparql()
            throws IOException {
        String json = IOUtils.toString(engine.executeSparql(selectQuery, SparqlEngine.SPARQL_JSON).getInputStream(),
            "UTF-8");
        // FIXME make more in-depth JSON validation
        Assert.assertTrue("JSON looks correct", json.contains("Marco Roos"));
    }


    @Test
    public final void testExecuteAskFalseSparql()
            throws IOException {
        String xml = IOUtils.toString(engine.executeSparql(askFalseQuery, SparqlEngine.SPARQL_XML).getInputStream(),
            "UTF-8");
        Assert.assertTrue("XML looks correct", xml.contains("false"));
    }


    @Test
    public final void testExecuteAskTrueSparql()
            throws IOException {
        String xml = IOUtils.toString(engine.executeSparql(askTrueQuery, SparqlEngine.SPARQL_XML).getInputStream(),
            "UTF-8");
        Assert.assertTrue("XML looks correct", xml.contains("true"));
    }


    @Test
    public void testDefaultFormat() {
        RDFFormat jpeg = new RDFFormat("JPEG", "image/jpeg", Charset.forName("UTF-8"), "jpeg", false, false);
        QueryResult res = engine.executeSparql(describeQuery, jpeg);
        Assert.assertEquals("RDF/XML is the default format", RDFFormat.RDFXML, res.getFormat());
        res = engine.executeSparql(constructQuery, jpeg);
        Assert.assertEquals("RDF/XML is the default format", RDFFormat.RDFXML, res.getFormat());
        res = engine.executeSparql(selectQuery, jpeg);
        Assert.assertEquals("SPARQL XML is the default format", SemanticMetadataService.SPARQL_XML, res.getFormat());
        res = engine.executeSparql(askTrueQuery, jpeg);
        Assert.assertEquals("SPARQL XML is the default format", SemanticMetadataService.SPARQL_XML, res.getFormat());
    }

}
