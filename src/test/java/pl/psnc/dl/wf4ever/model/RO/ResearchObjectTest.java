package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.model.BaseTest;

public class ResearchObjectTest extends BaseTest {

    private URI researchObjectUri;


    @Override
    @Before
    public void setUp() {
        super.setUp();
    }


    @Test
    public void testConstructor() {

    }


    @Test
    public void testCreate() {
        ResearchObject ro = ResearchObject.create(builder, URI.create("http://example.org/"));
    }
}
