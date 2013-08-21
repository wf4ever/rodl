package pl.psnc.dl.wf4ever.accesscontrol.model.dao;

import java.net.URI;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.accesscontrol.dicts.Role;
import pl.psnc.dl.wf4ever.accesscontrol.model.Permission;
import pl.psnc.dl.wf4ever.db.UserProfile;
import pl.psnc.dl.wf4ever.db.dao.UserProfileDAO;
import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
import pl.psnc.dl.wf4ever.model.BaseTest;

public class PermissionDAOTest extends BaseTest {

    String id;
    String roUri = "http://www.example.com/ROs/1/";
    URI userUri;
    UserProfileDAO userProfileDAO = new UserProfileDAO();
    Permission permission;
    PermissionDAO dao = new PermissionDAO();
    UserProfile profile;


    @Before
    public void setUp()
            throws Exception {
        super.setUp();
        id = "http://www.example.com/accesscontrol/permissions/" + UUID.randomUUID().toString();
        userUri = URI.create("http://testuser.myopenid.com/" + UUID.randomUUID().toString());
        profile = new UserProfile(userUri.toString(), "name", pl.psnc.dl.wf4ever.dl.UserMetadata.Role.AUTHENTICATED,
                userUri);
        userProfileDAO.save(profile);
        permission = new Permission();
        permission.setId(id);
        permission.setRoUri(roUri);
        permission.setRole(Role.EDITOR);
        permission.setUserId(profile);
    }


    @After
    public void tearDown()
            throws Exception {
        userProfileDAO.delete(profile);
        super.tearDown();
    }


    @Test
    public void testCRUD() {
        PermissionDAO dao = new PermissionDAO();
        dao.save(permission);
        permission = dao.findById(permission.getId());
        Assert.assertNotNull(permission);
        dao.delete(permission);
        permission = dao.findById(permission.getId());
        Assert.assertNull(permission);
    }


    @Test
    public void testUserProfileForeignKeyBehaviour() {
        permission.setUserId(null);
        dao.save(permission);
        try {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
            Assert.fail("expected ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().begin();
            return;
        }
    }
}