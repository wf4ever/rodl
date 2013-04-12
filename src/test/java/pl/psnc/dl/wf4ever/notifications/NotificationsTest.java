package pl.psnc.dl.wf4ever.notifications;

import java.net.URI;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.IntegrationTest;
import pl.psnc.dl.wf4ever.W4ETest;

@Category(IntegrationTest.class)
public class NotificationsTest extends W4ETest {

    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        createUserWithAnswer(userIdSafe, username).close();
        accessToken = createAccessToken(userId);
        //TODO get the resource path from service description
        webResource.path("notifications/").header("Authorization", "Bearer " + adminCreds).delete(String.class);
    }


    @Override
    public void tearDown()
            throws Exception {
        deleteROs();
        deleteAccessToken(accessToken);
        deleteUser(userIdSafe);
        super.tearDown();
    }


    @Test
    public void testDelete() {
        ro = createRO(accessToken);
        String before = (webResource.path("notifications/").get(String.class));
        webResource.path("notifications/").header("Authorization", "Bearer " + adminCreds).delete(String.class);
        String after = (webResource.path("notifications/").get(String.class));
        Assert.assertTrue(before.contains("entry"));
        Assert.assertFalse(after.contains("entry"));
    }


    @Test
    public void testFeed() {
        String resultAll = (webResource.path("notifications/").get(String.class));
    }


    @Test
    public void testNotificationsCreateNoFilters() {
        ro = createRO(accessToken);
        String resultAll = (webResource.path("notifications/").get(String.class));
        Assert.assertTrue(resultAll.contains(ro.toString()));
        Assert.assertTrue(resultAll.contains("urn:X-rodl:"));
        Assert.assertTrue(resultAll.contains("Research Object has been created"));
        Assert.assertTrue(resultAll
                .contains("&amp;lt;p&amp;gt;A new Research Object has been created.&amp;lt;/p&amp;gt;"));
        Assert.assertTrue(resultAll.contains("http://purl.org/dc/terms/source"));
        Assert.assertTrue(resultAll.contains("http://www.openarchives.org/ore/terms/describes"));
        Assert.assertTrue(resultAll.contains("Notifications for all ROs"));
    }


    @Test
    public void testNotificationsFilterRO() {
        ro = createRO(accessToken);
        ro2 = createRO(accessToken);
        String resultAll = (webResource.path("notifications/").queryParam("ro", ro.toString()).get(String.class));
        Assert.assertTrue(resultAll.contains("Notifications for " + ro.toString()));
        Assert.assertFalse(resultAll.contains("Notifications for " + ro2.toString()));
    }


    @Test
    public void testRanges()
            throws InterruptedException {
        DateTime start = DateTime.now();
        Thread.sleep(2000);
        ro = createRO(accessToken);
        Thread.sleep(2000);
        DateTime middle = DateTime.now();
        Thread.sleep(2000);
        ro2 = createRO(accessToken);
        Thread.sleep(2000);
        URI ro3 = createRO(accessToken);
        Thread.sleep(2000);
        DateTime end = DateTime.now();

        String beforeStart = webResource.path("notifications/").queryParam("to", start.toString()).get(String.class);
        String afterStart = webResource.path("notifications/").queryParam("from", start.toString()).get(String.class);
        String afterMiddle = webResource.path("notifications/").queryParam("from", middle.toString()).get(String.class);
        String beforeMiddle = webResource.path("notifications/").queryParam("to", middle.toString()).get(String.class);
        String afterEnd = webResource.path("notifications/").queryParam("from", end.toString()).get(String.class);
        String beforeEnd = webResource.path("notifications/").queryParam("to", end.toString()).get(String.class);

        Assert.assertFalse(beforeStart.contains(ro.toString()));
        Assert.assertFalse(beforeStart.contains(ro2.toString()));
        Assert.assertFalse(beforeStart.contains(ro3.toString()));

        Assert.assertTrue(afterStart.contains(ro.toString()));
        Assert.assertTrue(afterStart.contains(ro2.toString()));
        Assert.assertTrue(afterStart.contains(ro3.toString()));

        Assert.assertTrue(beforeMiddle.contains(ro.toString()));
        Assert.assertFalse(beforeMiddle.contains(ro2.toString()));
        Assert.assertFalse(beforeMiddle.contains(ro3.toString()));

        Assert.assertFalse(afterMiddle.contains(ro.toString()));
        Assert.assertTrue(afterMiddle.contains(ro2.toString()));
        Assert.assertTrue(afterMiddle.contains(ro3.toString()));

        Assert.assertTrue(beforeEnd.contains(ro.toString()));
        Assert.assertTrue(beforeEnd.contains(ro2.toString()));
        Assert.assertTrue(beforeEnd.contains(ro3.toString()));

        Assert.assertFalse(afterEnd.contains(ro.toString()));
        Assert.assertFalse(afterEnd.contains(ro2.toString()));
        Assert.assertFalse(afterEnd.contains(ro3.toString()));
    }
}
