package pl.psnc.dl.wf4ever.model.ORE;

import java.net.URI;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.vocabulary.ORE;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

public class ResourceMapTest extends BaseTest {

    private URI resourceMapUri;
    private ResourceMap resourceMap;


    @Override
    @Before
    public void setUp() {
        super.setUp();
        resourceMapUri = researchObject.getUri().resolve("resource-map");
        resourceMap = new ResourceMap(userProfile, dataset, true, researchObject, resourceMapUri) {

            @Override
            public ResearchObject getResearchObject() {
                //mock :) 
                return researchObject;
            }
        };

    }


    @Test
    public void testConstructor() {
        ResourceMap createdResourceMap = new ResourceMap(userProfile, researchObject, resourceMapUri) {

            @Override
            public ResearchObject getResearchObject() {
                return null;
            }
        };
        Assert.assertNotNull(createdResourceMap);
    }


    @Test
    public void testSave() {
        resourceMap.save();
        Model model = ModelFactory.createDefaultModel();
        model.read(resourceMap.getGraphAsInputStream(RDFFormat.RDFXML), null);

        Resource r = model.getResource(researchObject.getUri().toString());
        Assert.assertNotNull(r);
        Assert.assertNotNull(r.getProperty(DCTerms.creator));
        Assert.assertNotNull(r.getProperty(DCTerms.created));
        Assert.assertEquals(resourceMapUri.toString(), r.getPropertyResourceValue(ORE.isDescribedBy).getURI()
                .toString());

        r = model.getResource(resourceMapUri.toString());
        Assert.assertNotNull(r);
        Assert.assertEquals(researchObject.getUri().toString(), r.getPropertyResourceValue(ORE.describes).getURI()
                .toString());
        Assert.assertEquals(ORE.ResourceMap.getURI(), r.getPropertyResourceValue(RDF.type).getURI().toString());

    }
}
