package pl.psnc.dl.wf4ever.model.ORE;

import java.net.URI;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

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
        //TODO WHY ??
        resourceMap.save();
    }

}
