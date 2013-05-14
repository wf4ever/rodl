/**
 * 
 */
package pl.psnc.dl.wf4ever.sparql;

import java.net.URI;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author piotrhol
 * 
 */
public class RO_TurtleWriterTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass()
            throws Exception {
    }


    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass()
            throws Exception {
    }


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
            throws Exception {
    }


    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown()
            throws Exception {
    }


    /**
     * Test method for {@link pl.psnc.dl.wf4ever.sparql.RO_RDFXMLWriter#relativize(java.lang.String)}.
     */
    @Test
    public final void testRelativizeString() {
        RO_TurtleWriter writer = new RO_TurtleWriter();
        writer.setResearchObjectURI(URI.create("http://example.org/ROs/ro1/"));
        writer.setBaseURI(URI.create("http://example.org/ROs/ro1/base/"));

        Assert.assertEquals("<http://example.org/ROs/ro2/resource>",
            writer.formatURI("http://example.org/ROs/ro2/resource"));
        Assert.assertEquals("<resource>", writer.formatURI("http://example.org/ROs/ro1/base/resource"));
        Assert.assertEquals("<../resource>", writer.formatURI("http://example.org/ROs/ro1/resource"));
        Assert.assertEquals("<folder/resource>", writer.formatURI("http://example.org/ROs/ro1/base/folder/resource"));
        Assert.assertEquals("<../folder/resource>", writer.formatURI("http://example.org/ROs/ro1/folder/resource"));

        Assert.assertEquals("<../graph1>", writer.formatURI("http://example.org/ROs/ro1/graph1"));
        Assert.assertEquals("<folder/graph%202>", writer.formatURI("http://example.org/ROs/ro1/base/folder/graph%202"));

        Assert.assertEquals("<resource%20with%20spaces.txt>",
            writer.formatURI("http://example.org/ROs/ro1/base/resource%20with%20spaces.txt"));
    }

}
