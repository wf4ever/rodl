package pl.psnc.dl.wf4ever.db;

import java.net.URI;

import junit.framework.Assert;

import org.junit.Test;

public class ResearchObjectIdTest {

    URI idUri = URI.create("http://www.example.com/ROs/ResearchObject/");


    @Test
    public void testConstructor() {
        ResearchObjectId id = new ResearchObjectId(idUri);
        Assert.assertEquals(idUri, id.getId());
    }
}
