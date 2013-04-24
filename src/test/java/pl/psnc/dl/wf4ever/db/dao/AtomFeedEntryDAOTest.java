package pl.psnc.dl.wf4ever.db.dao;

import java.net.URI;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.notifications.Notification;

public class AtomFeedEntryDAOTest extends BaseTest {

    ResearchObject ro;
    URI roUri = URI.create("http://www.example.com/ROs/atomFeedTest");
    AtomFeedEntryDAO dao;


    @Override
    @Before
    public void setUp()
            throws Exception {
        super.setUp();
        ro = builder.buildResearchObject(roUri);
        dao = new AtomFeedEntryDAO();
        for (Notification entry : dao.find(roUri, null, null, null, null)) {
            dao.delete(entry);
        }
    }


    @Override
    @After
    public void tearDown()
            throws Exception {
        for (Notification entry : dao.find(roUri, null, null, null, null)) {
            dao.delete(entry);
        }
        super.tearDown();
    }


    @Test
    public void testConstructor() {
        AtomFeedEntryDAO daoT = new AtomFeedEntryDAO();
        Assert.assertNotNull(daoT);
    }


    @Test
    public void testAll() {
        for (Notification entry : dao.all()) {
            dao.delete(entry);
        }
        dao.save(new Notification.Builder(ro).build());
        dao.save(new Notification.Builder(ro).build());
        dao.save(new Notification.Builder(ro).build());
        dao.save(new Notification.Builder(ro).build());
        dao.save(new Notification.Builder(ro).build());
        Assert.assertEquals(5, dao.all().size());
    }


    @Test
    public void testSelect()
            throws InterruptedException {
        Thread.currentThread();
        DateTime start = DateTime.now();
        dao.save(new Notification.Builder(ro).build());
        Thread.sleep(1000);
        DateTime middle = DateTime.now();
        Thread.sleep(1000);
        dao.save(new Notification.Builder(ro).build());
        Thread.sleep(1000);
        dao.save(new Notification.Builder(ro).build());
        DateTime end = DateTime.now();

        Assert.assertEquals(3, dao.find(ro.getUri(), null, null, null, null).size());
        Assert.assertEquals(3, dao.find(ro.getUri(), start.toDate(), null, null, null).size());
        Assert.assertEquals(3, dao.find(ro.getUri(), null, end.toDate(), null, null).size());
        Assert.assertEquals(3, dao.find(ro.getUri(), start.toDate(), end.toDate(), null, null).size());
        Assert.assertEquals(2, dao.find(ro.getUri(), middle.toDate(), end.toDate(), null, null).size());
        Assert.assertEquals(1, dao.find(ro.getUri(), start.toDate(), middle.toDate(), null, null).size());
        Assert.assertEquals(0, dao.find(ro.getUri(), end.toDate(), start.toDate(), null, null).size());
    }

    //
    //    @Test
    //    public void testLimit() {
    //        dao.save(EntryBuilder.create(ro, ActionType.NEW_RO));
    //        dao.save(EntryBuilder.create(ro, ActionType.NEW_RO));
    //        dao.save(EntryBuilder.create(ro, ActionType.NEW_RO));
    //        Assert.assertEquals(1, dao.find(null, null, null, null, 1).size());
    //    }

}
