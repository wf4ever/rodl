package pl.psnc.dl.wf4ever.db;

import java.io.IOException;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;

public class UserProfileTest {

    String login = "login";
    String name = "name";
    Role role = Role.AUTHENTICATED;
    URI uri = URI.create("http://example.org/fakeUP/");


    @Test
    public void testConstructor()
            throws IOException {
        UserProfile profile = new UserProfile(login, name, role);
        Assert.assertEquals(login, profile.getLogin());
        Assert.assertEquals(name, profile.getName());
        Assert.assertEquals(role, profile.getRole());

        profile = new UserProfile(login, name, role, uri);
        Assert.assertEquals(login, profile.getLogin());
        Assert.assertEquals(name, profile.getName());
        Assert.assertEquals(role, profile.getRole());
        Assert.assertEquals(uri, profile.getUri());
    }
}
