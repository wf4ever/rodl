package pl.psnc.dl.wf4ever.db.dao;

import java.net.URI;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.db.AtomFeedEntry;
import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.notifications.ActionType;
import pl.psnc.dl.wf4ever.notifications.EntryBuilder;

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
        for (AtomFeedEntry entry : dao.all()) {
            dao.delete(entry);
        }
    }


    @Test
    public void testConstructor() {
        AtomFeedEntryDAO daoT = new AtomFeedEntryDAO();
        Assert.assertNotNull(daoT);
    }


    @Test
    public void testAll() {
        dao.save(EntryBuilder.create(researchObject, ActionType.NEW_RO));
        dao.save(EntryBuilder.create(researchObject, ActionType.NEW_RO));
        dao.save(EntryBuilder.create(researchObject, ActionType.NEW_RO));
        dao.save(EntryBuilder.create(researchObject, ActionType.NEW_RO));
        dao.save(EntryBuilder.create(researchObject, ActionType.NEW_RO));
        Assert.assertEquals(5, dao.all().size());
    }


    @Test
    public void testSelect()
            throws InterruptedException {
        Thread.currentThread();
        DateTime start = DateTime.now();
        dao.save(EntryBuilder.create(researchObject, ActionType.NEW_RO));
        Thread.sleep(1000);
        DateTime middle = DateTime.now();
        Thread.sleep(1000);
        dao.save(EntryBuilder.create(researchObject, ActionType.NEW_RO));
        Thread.sleep(1000);
        dao.save(EntryBuilder.create(researchObject, ActionType.NEW_RO));
        DateTime end = DateTime.now();

        Assert.assertEquals(3, dao.find(researchObject.getUri(), null, null).size());
        Assert.assertEquals(3, dao.find(null, null, null).size());
        Assert.assertEquals(3, dao.find(null, start.toDate(), null).size());
        Assert.assertEquals(3, dao.find(null, null, end.toDate()).size());
        Assert.assertEquals(3, dao.find(researchObject.getUri(), start.toDate(), end.toDate()).size());
        Assert.assertEquals(2, dao.find(researchObject.getUri(), middle.toDate(), end.toDate()).size());
        Assert.assertEquals(1, dao.find(researchObject.getUri(), start.toDate(), middle.toDate()).size());
        Assert.assertEquals(0, dao.find(researchObject.getUri(), end.toDate(), start.toDate()).size());
    }

}
