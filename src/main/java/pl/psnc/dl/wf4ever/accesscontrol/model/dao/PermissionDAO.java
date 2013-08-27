package pl.psnc.dl.wf4ever.accesscontrol.model.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import pl.psnc.dl.wf4ever.accesscontrol.model.Permission;
import pl.psnc.dl.wf4ever.db.dao.AbstractDAO;
import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;

/**
 * Research Object permission DAO.
 * 
 * @author pejot
 * 
 */
public final class PermissionDAO extends AbstractDAO<Permission> {

    /** id. */
    private static final long serialVersionUID = -4468344863067565271L;


    /**
     * Find permission by id.
     * 
     * @param id
     *            uri (id)
     * @return client or null
     */
    public Permission findById(Integer id) {
        return findByPrimaryKey(Permission.class, id);
    }


    /**
     * Find Access Control Mode by Research Object.
     * 
     * @param ro
     *            Research Object.
     * @return Access Control mode, null in case mode doesn't exists
     */
    public List<Permission> findByResearchObject(String ro) {
        Criteria criteria = HibernateUtil.getSessionFactory().getCurrentSession().createCriteria(Permission.class);
        criteria.add(Restrictions.eq("ro", ro));
        return criteria.list();

    }

}
