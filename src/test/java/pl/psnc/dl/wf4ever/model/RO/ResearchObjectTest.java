package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;
import java.util.Calendar;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.vocabulary.FOAF;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;

public class ResearchObjectTest extends BaseTest {

    private static final URI RESEARCH_OBJECT_TO_CREATE = URI.create("http://example.org/testro/");


    @Before
    public void setUp()
            throws Exception {
        super.setUp();
    }


    @After
    public void tearDown()
            throws Exception {
        super.tearDown();
    }


    /**
     * I tried to move the tests from SMS but there are errors where SMS is still used, so ignore for now.
     */
    @Test
    @Ignore
    public final void testDelete() {
        ResearchObject researchObject = ResearchObject.create(builder, RESEARCH_OBJECT_TO_CREATE);
        researchObject.delete();

        //Should not throw an exception
        researchObject.delete();

        Assert.assertFalse("Dependent named models should be deleted", dataset.containsNamedModel(ANNOTATION_BODY));
    }


    /**
     * I tried to move the tests from SMS but there are errors where SMS is still used, so ignore for now.
     */
    @Test
    @Ignore
    public final void testGetManifest() {
        Calendar before = Calendar.getInstance();
        ResearchObject researchObject = ResearchObject.create(builder, RESEARCH_OBJECT_TO_CREATE);
        Calendar after = Calendar.getInstance();
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read(researchObject.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);

        Individual manifest = model.getIndividual(researchObject.getManifestUri().toString());
        Individual ro = model.getIndividual(researchObject.getUri().toString());
        Assert.assertNotNull("Manifest must contain ro:Manifest", manifest);
        Assert.assertNotNull("Manifest must contain ro:ResearchObject", ro);
        Assert.assertTrue("Manifest must be a ro:Manifest", manifest.hasRDFType(RO.NAMESPACE + "Manifest"));
        Assert.assertTrue("RO must be a ro:ResearchObject", ro.hasRDFType(RO.NAMESPACE + "ResearchObject"));

        Literal createdLiteral = manifest.getPropertyValue(DCTerms.created).asLiteral();
        Assert.assertNotNull("Manifest must contain dcterms:created", createdLiteral);
        Assert.assertEquals("Date type is xsd:dateTime", XSDDatatype.XSDdateTime, createdLiteral.getDatatype());
        Calendar created = ((XSDDateTime) createdLiteral.getValue()).asCalendar();
        Assert.assertTrue("Created is a valid date", !before.after(created) && !after.before(created));
        Resource creatorResource = ro.getPropertyResourceValue(DCTerms.creator);
        Assert.assertNotNull("RO must contain dcterms:creator", creatorResource);

        Individual creator = model.getIndividual(creatorResource.getURI());
        Assert.assertTrue("Creator must be a foaf:Agent", creator.hasRDFType("http://xmlns.com/foaf/0.1/Agent"));
        Assert.assertEquals("Creator name must be correct", userProfile.getName(), creator.getPropertyValue(FOAF.name)
                .asLiteral().getString());
        researchObject.delete();
    }

}
