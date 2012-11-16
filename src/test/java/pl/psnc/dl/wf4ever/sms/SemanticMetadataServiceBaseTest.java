package pl.psnc.dl.wf4ever.sms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.common.ResearchObject;
import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class SemanticMetadataServiceBaseTest {

    @SuppressWarnings("unused")
    protected static final Logger log = Logger.getLogger(SemanticMetadataServiceImplTest.class);

    protected static ResearchObject researchObject;

    protected static ResearchObject researchObject2URI;

    protected static ResearchObject snapshotResearchObjectURI;

    protected static ResearchObject archiveResearchObjectURI;

    protected static ResearchObject wrongResearchObjectURI;

    protected static UserMetadata userProfile;

    protected final static URI workflowURI = URI.create("http://example.org/ROs/ro1/a%20workflow.t2flow");

    protected final static URI workflowPartURI = URI
            .create("http://example.org/ROs/ro1/a%20workflow.t2flow#somePartOfIt");

    protected final static URI workflow2URI = URI.create("http://example.org/ROs/ro2/runme.t2flow");

    protected static ResourceMetadata workflowInfo;

    protected final static URI ann1URI = URI.create("http://example.org/ROs/ro1/ann1");

    protected static ResourceMetadata ann1Info;

    protected final static URI resourceFakeURI = URI.create("http://example.org/ROs/ro1/xyz");

    protected static ResourceMetadata resourceFakeInfo;

    protected final static URI FOLDER_URI = URI.create("http://example.org/ROs/ro1/afolder/");

    protected final static URI annotationBody1URI = URI.create("http://example.org/ROs/ro1/.ro/ann1");

    protected TestStructure test;


    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass()
            throws Exception {

        researchObject = new ResearchObject(URI.create("http://example.org/ROs/ro1/"));
        researchObject2URI = new ResearchObject(URI.create("http://example.org/ROs/ro2/"));
        snapshotResearchObjectURI = new ResearchObject(URI.create("http://example.org/ROs/sp1/"));
        archiveResearchObjectURI = new ResearchObject(URI.create("http://example.org/ROs/arch1/"));
        wrongResearchObjectURI = new ResearchObject(URI.create("http://wrong.example.org/ROs/wrongRo/"));
        userProfile = new UserMetadata("jank", "Jan Kowalski", Role.AUTHENTICATED);
        workflowInfo = new ResourceMetadata("a%20workflow.t2flow", "a%20workflow.t2flow", "ABC123455666344E", 646365L,
                "SHA1", null, "application/vnd.taverna.t2flow+xml");
        ann1Info = new ResourceMetadata("ann1", "ann1", "A0987654321EDCB", 6L, "MD5", null, "application/rdf+xml");
        resourceFakeInfo = new ResourceMetadata("xyz", "xyz", "A0987654321EDCB", 6L, "MD5", null, "text/plain");

    }


    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass()
            throws Exception {
        cleanData();
    }


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
            throws Exception {
        cleanData();
        test = new TestStructure();
    }


    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown()
            throws Exception {
    }


    private static void cleanData() {
        SemanticMetadataService sms = null;
        try {
            sms = new SemanticMetadataServiceImpl(userProfile, true);
            try {
                sms.removeResearchObject(researchObject);
            } catch (IllegalArgumentException e) {
                // nothing
            }
            try {
                sms.removeResearchObject(researchObject2URI);
            } catch (IllegalArgumentException e) {
                // nothing
            }
            try {
                sms.removeResearchObject(snapshotResearchObjectURI);
            } catch (IllegalArgumentException e) {
                // nothing
            }
            try {
                sms.removeResearchObject(archiveResearchObjectURI);
            } catch (IllegalArgumentException e) {
                // nothing
            }
        } catch (ClassNotFoundException | IOException | NamingException | SQLException e) {
            e.printStackTrace();
        } finally {
            if (sms != null) {
                sms.close();
            }
        }
    }


    /***** HELPERS *****/

    protected Boolean isChangeInTheChangesList(String relatedObjectURI, String rdfClass, OntModel model,
            List<RDFNode> changesList) {
        for (RDFNode change : changesList) {
            Boolean partialResult1 = change.asResource()
                    .getProperty(model.createProperty("http://purl.org/wf4ever/roevo#relatedResource")).getObject()
                    .toString().equals(relatedObjectURI);
            Boolean partialREsult2 = change.as(Individual.class).hasRDFType(rdfClass);
            if (partialResult1 && partialREsult2) {
                return true;
            }
        }
        return false;
    }


    protected URI getResourceURI(String resourceName)
            throws URISyntaxException {
        String PROJECT_PATH = System.getProperty("user.dir");
        String FILE_SEPARATOR = System.getProperty("file.separator");
        String result = PROJECT_PATH;
        result += FILE_SEPARATOR + "src" + FILE_SEPARATOR + "test" + FILE_SEPARATOR + "resources" + FILE_SEPARATOR
                + "rdfStructure" + FILE_SEPARATOR + resourceName;
        return new URI("file://" + result);
    }


    class TestStructure {

        public ResearchObject ro1;
        public ResearchObject sp1;
        public ResearchObject sp2;
        public ResearchObject arch1;
        public ResearchObject wrongRO;
        public ResearchObject annotatedRO;
        public SemanticMetadataService sms;


        public TestStructure()
                throws URISyntaxException, FileNotFoundException {
            ro1 = ResearchObject.create(getResourceURI("ro1/"));
            sp1 = ResearchObject.create(getResourceURI("ro1-sp1/"));
            sp2 = ResearchObject.create(getResourceURI("ro1-sp2/"));
            arch1 = ResearchObject.create(getResourceURI("ro1-arch1/"));
            wrongRO = ResearchObject.create(getResourceURI("wrong-ro/"));
            annotatedRO = ResearchObject.create(URI.create("http://www.example.com/annotatedRO/"));

            InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/ro1/.ro/manifest.ttl");
            sms = new SemanticMetadataServiceImpl(userProfile, ro1, is, RDFFormat.TURTLE);

            is = getClass().getClassLoader().getResourceAsStream("rdfStructure/ro1/.ro/evo_info.ttl");
            sms.addNamedGraph(ro1.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);

            is = getClass().getClassLoader().getResourceAsStream("rdfStructure/ro1-sp1/.ro/manifest.ttl");
            sms.createResearchObject(sp1);
            sms.updateManifest(sp1, is, RDFFormat.TURTLE);
            is = getClass().getClassLoader().getResourceAsStream("rdfStructure/ro1-sp1/.ro/evo_info.ttl");
            sms.addNamedGraph(sp1.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);

            is = getClass().getClassLoader().getResourceAsStream("rdfStructure/ro1-sp2/.ro/manifest.ttl");
            sms.createResearchObject(sp2);
            sms.updateManifest(sp2, is, RDFFormat.TURTLE);
            is = getClass().getClassLoader().getResourceAsStream("rdfStructure/ro1-sp2/.ro/evo_info.ttl");
            sms.addNamedGraph(sp2.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);

            is = getClass().getClassLoader().getResourceAsStream("rdfStructure/ro1-arch1/.ro/manifest.ttl");
            sms.createResearchObject(arch1);
            sms.updateManifest(arch1, is, RDFFormat.TURTLE);
            is = getClass().getClassLoader().getResourceAsStream("rdfStructure/ro1-arch1/.ro/evo_info.ttl");
            sms.addNamedGraph(arch1.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);

            is = getClass().getClassLoader().getResourceAsStream("rdfStructure/wrong-ro/.ro/manifest.ttl");
            sms.createResearchObject(wrongRO);
            sms.updateManifest(wrongRO, is, RDFFormat.TURTLE);
            is = getClass().getClassLoader().getResourceAsStream("rdfStructure/wrong-ro/.ro/evo_info.ttl");
            sms.addNamedGraph(wrongRO.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);

            is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/manifest.ttl");
            sms.createResearchObject(annotatedRO);
            sms.updateManifest(annotatedRO, is, RDFFormat.TURTLE);
            is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/evo_info.ttl");
            sms.addNamedGraph(annotatedRO.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);
            sms.addResource(annotatedRO, workflowURI, workflowInfo);
        }
    }
}
