package pl.psnc.dl.wf4ever.monitoring;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.IntegrationTest;
import pl.psnc.dl.wf4ever.W4ETest;

@Category(IntegrationTest.class)
public class ChecksumDetectionIntegrationTest extends W4ETest {

    @Before
    public void setUp()
            throws Exception {
        super.setUp();
        createUserWithAnswer(userIdSafe, username).close();
        accessToken = createAccessToken(userId);
        ro = createRO(accessToken);
    }


    @After
    public void tearDown()
            throws Exception {
        deleteROs();
        deleteAccessToken(accessToken);
        deleteUser(userIdSafe);
        super.tearDown();
    }


    @Test
    public final void test() {
    }

}
