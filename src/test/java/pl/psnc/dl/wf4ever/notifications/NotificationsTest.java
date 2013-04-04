package pl.psnc.dl.wf4ever.notifications;

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
        ro = createRO(accessToken);
    }


    @Override
    public void tearDown()
            throws Exception {
        deleteROs();
        deleteAccessToken(accessToken);
        deleteUser(userIdSafe);
    }


    @Test
    public void testCreateNotification() {
        System.out.println(webResource.path("notifications/notifications").get(String.class));
    }
}
