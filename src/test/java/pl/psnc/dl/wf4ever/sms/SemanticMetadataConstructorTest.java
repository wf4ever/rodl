package pl.psnc.dl.wf4ever.sms;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

import javax.naming.NamingException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.common.ResearchObject;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.DCTerms;

public class SemanticMetadataConstructorTest extends SemanticMetadataServiceBaseTest {

    /**
     * Test method for
     * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#SemanticMetadataServiceImpl(pl.psnc.dl.wf4ever.dlibra.UserProfile)}
     * .
     * 
     * @throws SQLException
     * @throws NamingException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Test
    public final void testSemanticMetadataServiceImpl()
            throws ClassNotFoundException, IOException, NamingException, SQLException {
        SemanticMetadataService sms = new SemanticMetadataServiceTdb(userProfile, true);
        ResearchObject researchObject = ResearchObject.create(URI.create("http://www.example.com/testSMSImpl1/"));
        sms.removeResearchObject(researchObject);
        sms.createResearchObject(researchObject);
        try {
            sms.addResource(researchObject, researchObject.getUri().resolve(WORKFLOW_PATH), workflowInfo);
            sms.addResource(researchObject, researchObject.getUri().resolve(ANNOTATION_PATH), ann1Info);
        } finally {
            sms.removeResearchObject(researchObject);
            sms.close();
        }
        researchObject = ResearchObject.create(URI.create("http://www.example.com/testSMSImpl2/"));
        SemanticMetadataService sms2 = new SemanticMetadataServiceImpl(userProfile, true);
        sms2.removeResearchObject(researchObject);
        sms2.createResearchObject(researchObject);
        try {
            OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);

            model.read(sms2.getManifest(researchObject, RDFFormat.RDFXML), "");
            Individual manifest = model.getIndividual(researchObject.getManifestUri().toString());
            Individual ro = model.getIndividual(researchObject.getUri().toString());
            Assert.assertNotNull("Manifest must contain ro:Manifest", manifest);
            Assert.assertNotNull("Manifest must contain ro:ResearchObject", ro);
            Assert.assertTrue("Manifest must be a ro:Manifest", manifest.hasRDFType(RO.NAMESPACE + "Manifest"));
            Assert.assertTrue("RO must be a ro:ResearchObject", ro.hasRDFType(RO.NAMESPACE + "ResearchObject"));

            Literal createdLiteral = manifest.getPropertyValue(DCTerms.created).asLiteral();
            Assert.assertNotNull("Manifest must contain dcterms:created", createdLiteral);

            //          Resource creatorResource = manifest.getPropertyResourceValue(DCTerms.creator);
            //          Assert.assertNotNull("Manifest must contain dcterms:creator", creatorResource);
        } finally {
            sms2.removeResearchObject(researchObject);
            sms2.close();
        }

    }


    /**
     * SMS should be able to load Ontology.
     */
    @Test
    public final void testSMSConstructor()
            throws ClassNotFoundException, IOException, NamingException, SQLException, URISyntaxException {
        URI fakeURI = new URI("http://www.example.com/ROs/");
        InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/manifest.rdf");
        SemanticMetadataService sms = new SemanticMetadataServiceTdb(userProfile, ResearchObject.create(fakeURI), is,
                RDFFormat.RDFXML);
        try {
            String manifest = IOUtils.toString(sms.getManifest(
                ResearchObject.create(new URI("http://www.example.com/ROs/")), RDFFormat.RDFXML));
            Assert.assertTrue(manifest.contains("http://www.example.com/ROs/"));
            Assert.assertTrue(manifest.contains("Marco Roos"));
            Assert.assertTrue(manifest.contains("t2flow workflow annotation extractor"));
        } finally {
            sms.close();
        }
    }
}
