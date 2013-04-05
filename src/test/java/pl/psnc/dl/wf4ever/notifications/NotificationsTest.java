package pl.psnc.dl.wf4ever.notifications;

import junit.framework.Assert;

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
}
