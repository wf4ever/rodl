package pl.psnc.dl.wf4ever.sms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.common.HibernateUtil;
import pl.psnc.dl.wf4ever.common.ResearchObject;
import pl.psnc.dl.wf4ever.common.ResourceInfo;
import pl.psnc.dl.wf4ever.common.UserProfile;
import pl.psnc.dl.wf4ever.common.UserProfile.Role;

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

    protected static UserProfile userProfile;

    protected final static URI workflowURI = URI.create("http://example.org/ROs/ro1/a%20workflow.t2flow");

    protected final static URI workflowPartURI = URI
            .create("http://example.org/ROs/ro1/a%20workflow.t2flow#somePartOfIt");

    protected final static URI workflow2URI = URI.create("http://example.org/ROs/ro2/runme.t2flow");

    protected static ResourceInfo workflowInfo;

    protected final static URI ann1URI = URI.create("http://example.org/ROs/ro1/ann1");

    protected static ResourceInfo ann1Info;

    protected final static URI resourceFakeURI = URI.create("http://example.org/ROs/ro1/xyz");

    protected static ResourceInfo resourceFakeInfo;

    protected final static URI FOLDER_URI = URI.create("http://example.org/ROs/ro1/afolder/");

    protected final static URI annotationBody1URI = URI.create("http://example.org/ROs/ro1/.ro/ann1");

    protected static final String PROJECT_PATH = System.getProperty("user.dir");

    protected static final String FILE_SEPARATOR = System.getProperty("file.separator");

    protected TestStructure testStructure;


    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass()
            throws Exception {
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        researchObject = ResearchObject.create(URI.create("http://example.org/ROs/ro1/"));
        researchObject2URI = ResearchObject.create(URI.create("http://example.org/ROs/ro2/"));
        snapshotResearchObjectURI = ResearchObject.create(URI.create("http://example.org/ROs/sp1/"));
        archiveResearchObjectURI = ResearchObject.create(URI.create("http://example.org/ROs/arch1/"));
        wrongResearchObjectURI = ResearchObject.create(URI.create("http://wrong.example.org/ROs/wrongRo/"));
        userProfile = UserProfile.create("jank", "Jan Kowalski", Role.AUTHENTICATED);
        workflowInfo = ResourceInfo.create("a%20workflow.t2flow", "a%20workflow.t2flow", "ABC123455666344E", 646365L,
            "SHA1", null, "application/vnd.taverna.t2flow+xml");
        ann1Info = ResourceInfo.create("ann1", "ann1", "A0987654321EDCB", 6L, "MD5", null, "application/rdf+xml");
        resourceFakeInfo = ResourceInfo.create("xyz", "xyz", "A0987654321EDCB", 6L, "MD5", null, "text/plain");
    }


    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass()
            throws Exception {
        cleanData();
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
    }


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
            throws Exception {
        cleanData();
        testStructure = new TestStructure();
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
        public ResearchObject messRO;

        public SemanticMetadataService sms;


        public TestStructure()
                throws URISyntaxException, FileNotFoundException {
            ro1 = ResearchObject.create(getResourceURI("ro1/"));
            sp1 = ResearchObject.create(getResourceURI("ro1-sp1/"));
            sp2 = ResearchObject.create(getResourceURI("ro1-sp2/"));
            arch1 = ResearchObject.create(getResourceURI("ro1-arch1/"));
            wrongRO = ResearchObject.create(getResourceURI("wrong-ro/"));
            messRO = ResearchObject.create(getResourceURI("mess-ro/"));
            File file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1/.ro/manifest.ttl");
            FileInputStream is = new FileInputStream(file);
            sms = new SemanticMetadataServiceImpl(userProfile, ro1, is, RDFFormat.TURTLE);
            file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1/.ro/evo_info.ttl");
            is = new FileInputStream(file);
            sms.addNamedGraph(ro1.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);

            file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1-sp1/.ro/manifest.ttl");
            is = new FileInputStream(file);
            sms.createResearchObject(sp1);
            sms.updateManifest(sp1, is, RDFFormat.TURTLE);
            file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1-sp1/.ro/evo_info.ttl");
            is = new FileInputStream(file);
            sms.addNamedGraph(sp1.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);

            file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1-sp2/.ro/manifest.ttl");
            is = new FileInputStream(file);
            sms.createResearchObject(sp2);
            sms.updateManifest(sp2, is, RDFFormat.TURTLE);
            file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1-sp2/.ro/evo_info.ttl");
            is = new FileInputStream(file);
            sms.addNamedGraph(sp2.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);

            file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1-arch1/.ro/manifest.ttl");
            is = new FileInputStream(file);
            sms.createResearchObject(arch1);
            sms.updateManifest(arch1, is, RDFFormat.TURTLE);
            file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1-arch1/.ro/evo_info.ttl");
            is = new FileInputStream(file);
            sms.addNamedGraph(arch1.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);

            file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/wrong-ro/.ro/manifest.ttl");
            is = new FileInputStream(file);
            sms.createResearchObject(wrongRO);
            sms.updateManifest(wrongRO, is, RDFFormat.TURTLE);
            file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/wrong-ro/.ro/evo_info.ttl");
            is = new FileInputStream(file);
            sms.addNamedGraph(wrongRO.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);

            file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/wrong-ro/.ro/manifest.ttl");
            is = new FileInputStream(file);
            sms.createResearchObject(wrongRO);
            sms.updateManifest(wrongRO, is, RDFFormat.TURTLE);
            file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/wrong-ro/.ro/evo_info.ttl");
            is = new FileInputStream(file);
            sms.addNamedGraph(wrongRO.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);

            file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/mess-ro/.ro/manifest.ttl");
            is = new FileInputStream(file);
            sms.createResearchObject(messRO);
            sms.updateManifest(messRO, is, RDFFormat.TURTLE);
            file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/mess-ro/.ro/evo_info.ttl");
            is = new FileInputStream(file);
            sms.addNamedGraph(messRO.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);
        }
    }


    //not a real test.
    //@Test
    public final void generateRDF()
            throws URISyntaxException, IOException {
        URI fakeURI = new URI("http://www.example.com/ROs/");
        File file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1/.ro/manifest.ttl");
        FileInputStream is = new FileInputStream(file);
        SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, ResearchObject.create(fakeURI), is,
                RDFFormat.TURTLE);
        try {
            ResearchObject researchObject = ResearchObject.create(fakeURI);
            System.out.println(IOUtils.toString(sms.getNamedGraphWithRelativeURIs(researchObject.getManifestUri(),
                researchObject, RDFFormat.RDFXML)));
        } finally {
            sms.close();
        }
    }
}
