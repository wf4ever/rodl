package pl.psnc.dl.wf4ever.monitoring;

import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.IntegrationTest;
import pl.psnc.dl.wf4ever.W4ETest;
import pl.psnc.dl.wf4ever.darceo.client.DArceoClient;
import pl.psnc.dl.wf4ever.darceo.client.DArceoException;

import com.sun.syndication.io.FeedException;

/**
 * Check that checksum mismatches are reported as notifications.
 * 
 * @author piotrekhol
 * 
 */
@Category(IntegrationTest.class)
public class PreservationIntegrationTest extends W4ETest {

    /** A sample file name. */
    private String filePath = "foo.txt";


    @Before
    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        createUserWithAnswer(userIdSafe, username).close();
        accessToken = createAccessToken(userId);
        deleteROs();
        ro = createRO(accessToken);
        addFile(ro, filePath, accessToken);
    }


    @After
    @Override
    public void tearDown()
            throws Exception {
        deleteROs();
        deleteAccessToken(accessToken);
        deleteUser(userIdSafe);
        super.tearDown();
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
     */
    @Test
    @SuppressWarnings("unchecked")
    public final void test()
            throws FeedException, IOException, InterruptedException, DArceoException {

        //force scheduller
        webResource.path("admin/monitor/all").header("Authorization", "Bearer " + accessToken).post();

        Properties properties = new Properties();
        properties.load(SynchronizationTest.class.getClassLoader().getResourceAsStream("connection.properties"));
        if (properties.getProperty("repository_url") == null || properties.getProperty("repository_url").equals("")) {
            System.out.println("repository url not specified SynchronizationTest skipped");
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
        Assert.assertFalse("Object " + ro + " wasn't appear in dArceo", true);
    }

}
