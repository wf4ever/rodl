package pl.psnc.dl.wf4ever.triplestore;

import java.net.URI;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.vocabulary.FOAF;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.DCTerms;

public class ResourceTest extends BaseTest {

    @Override
    @Before
    public void setUp()
            throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
    }


    @Override
    @After
    public void tearDown()
            throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
    }


    @Test
    public void testFulfillCreatorNames() {
        ResearchObject ro = ResearchObject.create(builder,
            URI.create("http://example.org/ROs/testFulfillCreatorNames/"));
        OntModel manifestModel = ModelFactory.createOntologyModel();
        manifestModel.read(ro.getManifest().getGraphAsInputStream(RDFFormat.RDFXML), null);
        for (RDFNode n : manifestModel.listObjectsOfProperty(DCTerms.creator).toList()) {
            Assert.assertNull(manifestModel.getProperty(manifestModel.getResource(n.toString()), FOAF.name));
        }
        ro.delete();
    }
}
