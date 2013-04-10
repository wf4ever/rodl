package pl.psnc.dl.wf4ever.db;

import org.junit.Assert;
import org.junit.Test;

public class OAuthClientTest {

    String id = "id";
    String name = "name";
    String uri = "http://www.example.com/some-clinet-uri";


    @Test
    public void testConstructor() {
        OAuthClient client = new OAuthClient(id, name, uri);
        Assert.assertEquals(id, client.getClientId());
        Assert.assertEquals(name, client.getName());
        Assert.assertEquals(uri, client.getRedirectionURI());
    }
}
