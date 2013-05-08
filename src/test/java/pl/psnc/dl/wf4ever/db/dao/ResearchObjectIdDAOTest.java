package pl.psnc.dl.wf4ever.db.dao;

import java.net.URI;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.db.ResearchObjectId;
import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;

public class ResearchObjectIdDAOTest {

    URI idUri = URI.create("http://www.example.com/ROs/ResearchObject/");
    URI idUri2 = URI.create("http://www.example.com/ROs/ResearchObject2/");
    URI idUri3 = URI.create("http://www.example.com/ROs/ResearchObject3/");
    URI idUri11 = URI.create("http://www.example.com/ROs/ResearchObject-1/");
    URI idUri12 = URI.create("http://www.example.com/ROs/ResearchObject-2/");
    ResearchObjectId researchObjectId;
    ResearchObjectId researchObjectId2;
    ResearchObjectId researchObjectId3;
    ResearchObjectId researchObjectId11;
    ResearchObjectId researchObjectId12;
    ResearchObjectIdDAO dao;


    @Before
    public void setUp() {
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        dao = new ResearchObjectIdDAO();
        researchObjectId = new ResearchObjectId(idUri);
        researchObjectId2 = new ResearchObjectId(idUri2);
        researchObjectId3 = new ResearchObjectId(idUri3);
        researchObjectId11 = new ResearchObjectId(idUri11);
        researchObjectId12 = new ResearchObjectId(idUri12);
        clean(researchObjectId);
        clean(researchObjectId2);
        clean(researchObjectId3);
        clean(researchObjectId11);
        clean(researchObjectId12);
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
    }


    @After
    public void tearDown() {
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        clean(researchObjectId);
        clean(researchObjectId2);
        clean(researchObjectId3);
        clean(researchObjectId11);
        clean(researchObjectId12);
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
    }


    @Test
    public void testConstructor() {
        ResearchObjectIdDAO testDao = new ResearchObjectIdDAO();
        Assert.assertNotNull(testDao);
    }


    @Test
    public void testfisrtFree() {
        researchObjectId = new ResearchObjectId(idUri);
        Assert.assertEquals(idUri, dao.fistFree(researchObjectId).getId());
        dao.saftySave(researchObjectId);
        Assert.assertEquals(idUri11, dao.fistFree(researchObjectId).getId());
        dao.saftySave(researchObjectId);
        Assert.assertEquals(idUri12, dao.fistFree(researchObjectId).getId());
    }


    @Test
    public void testSaftySave() {
        //not this same instance
        researchObjectId = new ResearchObjectId(idUri);
        Assert.assertFalse(researchObjectId.equals(dao.fistFree(researchObjectId).getId()));
        dao.saftySave(researchObjectId);
        Assert.assertFalse(researchObjectId.equals(dao.fistFree(researchObjectId).getId()));
    }


    @Test
    public void testSave() {
        researchObjectId = new ResearchObjectId(idUri);
        Assert.assertNull(dao.findByPrimaryKey(researchObjectId.getId()));
        dao.save(researchObjectId);
        Assert.assertNotNull(dao.findByPrimaryKey(researchObjectId.getId()));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testSaveReplication() {
        researchObjectId = new ResearchObjectId(idUri);
        dao.save(researchObjectId);
        dao.save(researchObjectId);
    }


    private void clean(ResearchObjectId instance) {
        if (dao.findByPrimaryKey(instance.getId()) != null) {
            dao.delete(dao.findByPrimaryKey(instance.getId()));
        }
    }
}
