package pl.psnc.dl.wf4ever.accesscontrol.model.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import pl.psnc.dl.wf4ever.accesscontrol.dicts.Role;
import pl.psnc.dl.wf4ever.accesscontrol.model.Permission;
import pl.psnc.dl.wf4ever.accesscontrol.model.PermissionLink;
import pl.psnc.dl.wf4ever.db.UserProfile;
import pl.psnc.dl.wf4ever.db.dao.AbstractDAO;
import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;

/**
 * Research Object permission DAO.
 * 
 * @author pejot
 * 
 */
public final class PermissionLinkDAO extends AbstractDAO<PermissionLink> {

    /** id. */
    private static final long serialVersionUID = -4468344863067565271L;


    /**
     * Find permission link by id.
     * 
     * @param id
     *            uri (id)
     * @return client or null
     */
    public PermissionLink findById(Integer id) {
        return findByPrimaryKey(PermissionLink.class, id);
    }


    /**
     * Find Access Control Permission Links by Research Object.
     * 
     * @param ro
     *            Research Object.
     * @return Access Control mode, null in case mode doesn't exists
     */
    @SuppressWarnings("unchecked")
    public List<PermissionLink> findByResearchObject(String ro) {
        Criteria criteria = HibernateUtil.getSessionFactory().getCurrentSession().createCriteria(Permission.class);
        criteria.add(Restrictions.eq("ro", ro));
        return criteria.list();

    }


    /**
     * Find Permissions Links by user and research object uri.
     * 
     * @param user
     *            user profile
     * @param ro
     *            research object uri
     * @return list of granted permissions.
     */
    @SuppressWarnings("unchecked")
    public List<PermissionLink> findByUserROAndPermission(UserProfile user, String ro, Role role) {
        Criteria criteria = HibernateUtil.getSessionFactory().getCurrentSession().createCriteria(Permission.class);
        criteria.add(Restrictions.eq("ro", ro));
        criteria.add(Restrictions.eq("user", user));
        criteria.add(Restrictions.eq("role", role));
        return criteria.list();
    }

}
