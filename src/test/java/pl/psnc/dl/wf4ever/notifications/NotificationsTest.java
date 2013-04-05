package pl.psnc.dl.wf4ever.notifications;

import java.net.URI;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Test;

import pl.psnc.dl.wf4ever.W4ETest;

public class NotificationsTest extends W4ETest {

    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        createUserWithAnswer(userIdSafe, username).close();
        accessToken = createAccessToken(userId);
        webResource.path("notifications/notifications").header("Authorization", "Bearer " + adminCreds)
                .delete(String.class);
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
        String before = (webResource.path("notifications/notifications").get(String.class));
        webResource.path("notifications/notifications").header("Authorization", "Bearer " + adminCreds)
                .delete(String.class);
        String after = (webResource.path("notifications/notifications").get(String.class));
        Assert.assertTrue(before.contains("entry"));
        Assert.assertFalse(after.contains("entry"));
    }


    @Test
    public void testFeed() {
        String resultAll = (webResource.path("notifications/notifications").get(String.class));
    }


    @Test
    public void testNotificationsCreateNoFilters() {
        ro = createRO(accessToken);
        String resultAll = (webResource.path("notifications/notifications").get(String.class));
        Assert.assertTrue(resultAll.contains(ro.toString()));
        Assert.assertTrue(resultAll.contains("urn:X-rodl:"));
        Assert.assertTrue(resultAll.contains("Research Object Created"));
        Assert.assertTrue(resultAll.contains("The new Research Object was created"));
        Assert.assertTrue(resultAll.contains("http://purl.org/dc/terms/source"));
        Assert.assertTrue(resultAll.contains("http://www.openarchives.org/ore/terms/describes"));
        Assert.assertTrue(resultAll.contains("Notifications for all ROs"));
    }


    @Test
    public void testNotificationsFilterRO() {
        ro = createRO(accessToken);
        ro2 = createRO(accessToken);
        String resultAll = (webResource.path("notifications/notifications").queryParam("ro", ro.toString())
                .get(String.class));
        Assert.assertTrue(resultAll.contains("Notifications for " + ro.toString()));
        Assert.assertFalse(resultAll.contains("Notifications for " + ro2.toString()));
    }


    @SuppressWarnings("static-access")
    @Test
    public void testRanges()
            throws InterruptedException {
        DateTime start = DateTime.now();
        ro = createRO(accessToken);
        Thread.currentThread().sleep(1000);
        DateTime middle = DateTime.now();
        ro2 = createRO(accessToken);
        Thread.currentThread().sleep(1000);
        URI ro3 = createRO(accessToken);
        DateTime end = DateTime.now();

        String beforeStart = webResource.path("notifications/notifications").queryParam("to", start.toString())
                .get(String.class);
        String afterStart = webResource.path("notifications/notifications").queryParam("from", start.toString())
                .get(String.class);
        String afterMiddle = webResource.path("notifications/notifications").queryParam("from", middle.toString())
                .get(String.class);
        String beforeMiddle = webResource.path("notifications/notifications").queryParam("to", middle.toString())
                .get(String.class);
        String afterEnd = webResource.path("notifications/notifications").queryParam("from", end.toString())
                .get(String.class);
        String beforeEnd = webResource.path("notifications/notifications").queryParam("to", end.toString())
                .get(String.class);

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
