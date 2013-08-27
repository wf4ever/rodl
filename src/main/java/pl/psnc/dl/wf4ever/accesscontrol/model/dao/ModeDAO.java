package pl.psnc.dl.wf4ever.accesscontrol.model.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import pl.psnc.dl.wf4ever.accesscontrol.model.Mode;
import pl.psnc.dl.wf4ever.db.dao.AbstractDAO;
import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;

/**
 * Research Object access mode DAO.
 * 
 * @author pejot
 * 
 */
public final class ModeDAO extends AbstractDAO<Mode> {

    /** id. */
    private static final long serialVersionUID = -4468344863067565271L;


    /**
     * Find Access Control Mode by id.
     * 
     * @param id
     *            uri (id)
     * @return client or null
     */
    public Mode findById(Integer id) {
        return findByPrimaryKey(Mode.class, id);
    }


    /**
     * Find Access Control Mode by Research Object.
     * 
     * @param ro
     *            Research Object.
     * @return Access Control mode, null in case mode doesn't exists
     */
    public Mode findByResearchObject(String ro) {
        Criteria criteria = HibernateUtil.getSessionFactory().getCurrentSession().createCriteria(Mode.class);
        criteria.add(Restrictions.eq("ro", ro));
        List<Mode> result = criteria.list();
        if (result.size() == 1) {
            return result.get(0);
        }
        return null;
    }
}
