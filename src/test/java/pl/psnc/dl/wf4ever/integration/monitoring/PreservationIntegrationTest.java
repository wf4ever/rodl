package pl.psnc.dl.wf4ever.integration.monitoring;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.darceo.client.DArceoClient;
import pl.psnc.dl.wf4ever.darceo.client.DArceoException;
import pl.psnc.dl.wf4ever.integration.AbstractIntegrationTest;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;
import pl.psnc.dl.wf4ever.monitoring.SynchronizationTest;

import com.sun.syndication.io.FeedException;

/**
 * Check that checksum mismatches are reported as notifications.
 * 
 * @author piotrekhol
 * 
 */
@Category(IntegrationTest.class)
public class PreservationIntegrationTest extends AbstractIntegrationTest {

    /** A sample file name. */
    private String filePath = "foo.txt";
    private URI ro;
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(PreservationIntegrationTest.class);


    @Before
    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        ro = createRO();
        addLoremIpsumFile(ro, filePath);
    }


    /**
     * Modify a checksum in the database, force monitoring and check that a notification is generated.
     * 
     * @throws FeedException
     *             can't load the feed
     * @throws IOException
     *             can't load the feed
     * @throws InterruptedException
     *             interrupted when waiting for the notifications
     * @throws DArceoException
     *             when it can't connect to dArceo
     */
    @Test
    public final void test()
            throws FeedException, IOException, InterruptedException, DArceoException {

        //force scheduler
        webResource.path("admin/monitor/all").header("Authorization", "Bearer " + accessToken).post();

        Properties properties = new Properties();
        properties.load(SynchronizationTest.class.getClassLoader().getResourceAsStream("connection.properties"));
        if (properties.getProperty("repository_url") == null || properties.getProperty("repository_url").isEmpty()) {
            LOGGER.debug("repository url not specified SynchronizationTest skipped");
            return;
        }

        int times = 0;
        while (++times < 10) {
            Thread.sleep(10000);
            if (DArceoClient.getInstance().getBlocking(ro) != null) {
                DArceoClient.getInstance().delete(ro);
                return;
            }

        }
        Assert.fail("Object " + ro + " didn't appear in dArceo");
    }

}
