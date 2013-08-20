package pl.psnc.dl.wf4ever.db.dao;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.AbstractUnitTest;
import pl.psnc.dl.wf4ever.db.AccessToken;
import pl.psnc.dl.wf4ever.db.OAuthClient;
import pl.psnc.dl.wf4ever.db.UserProfile;
import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;

public class AccessTokenDAOTest extends AbstractUnitTest {

    AccessTokenDAO dao;
    UserProfile profile1;
    UserProfile profile2;
    OAuthClient client1;
    OAuthClient client2;
    UserProfileDAO userProfileDAO;
    OAuthClientDAO oAuthClientDAO;
    String token1val = "token1";
    String token2val = "token2";


    @Override
    @Before
    public void setUp()
            throws Exception {
        super.setUp();
        dao = new AccessTokenDAO();
        userProfileDAO = new UserProfileDAO();
        oAuthClientDAO = new OAuthClientDAO();
        profile1 = new UserProfile("logi1n", "name1", Role.AUTHENTICATED);
        profile2 = new UserProfile("login2", "name2", Role.AUTHENTICATED);
        userProfileDAO.save(profile1);
        userProfileDAO.save(profile2);
        client1 = new OAuthClient("name1", "http://www.example.org/redirect1");
        client2 = new OAuthClient("name1", "http://www.example.org/redirect1");
        oAuthClientDAO.save(client1);
        oAuthClientDAO.save(client2);
    }


    @Override
    @After
    public void tearDown()
            throws Exception {
        userProfileDAO.delete(profile1);
        userProfileDAO.delete(profile2);
        oAuthClientDAO.delete(client1);
        oAuthClientDAO.delete(client2);
        super.tearDown();
    }


    @Test
    public void testConstructor() {
        AccessTokenDAO daoT = new AccessTokenDAO();
        Assert.assertNotNull(daoT);
    }


    @Test
    public void testFind() {

        AccessToken token1 = new AccessToken(token1val, client1, profile1);
        AccessToken token2 = new AccessToken(token2val, client2, profile2);
        dao.save(token1);
        dao.save(token2);

        Assert.assertEquals(token1, dao.findByValue(token1val));
        Assert.assertEquals(token2, dao.findByValue(token2val));
        dao.findByClientOrUser(client1, null);
        Assert.assertEquals(1, dao.findByClientOrUser(client1, profile1).size());
        Assert.assertEquals(0, dao.findByClientOrUser(client1, profile2).size());

        Assert.assertEquals(token2, dao.findByValue(token2val));

        dao.delete(token1);
        dao.delete(token2);
    }
}
