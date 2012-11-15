package pl.psnc.dl.wf4ever.common;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;

/**
 * Active Record instance, with generic methods for loading, saving and deleting objects.
 * 
 * @author piotrek
 * 
 */
public abstract class ActiveRecord implements Serializable {

    /** id. */
    private static final long serialVersionUID = 258330617173783552L;


    /**
     * Load an object.
     * 
     * @param <T>
     *            ActiveRecord class
     * @param clazz
     *            ActiveRecord class instance
     * @param id
     *            primary key
     * @return an object or null
     */
    @SuppressWarnings("unchecked")
    protected static <T extends ActiveRecord> T findByPrimaryKey(Class<T> clazz, Serializable id) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        if (session.getTransaction().isActive()) {
            T activeRecord = (T) session.get(clazz, id);
            return activeRecord;
        } else {
            return null;
        }
    }


    /**
     * Find all objects.
     * 
     * @param <T>
     *            Active Record class
     * @param clazz
     *            Active Record class instance
     * @return a list of all objects
     */
    protected static <T extends ActiveRecord> List<T> findAll(Class<T> clazz) {
        return findByCriteria(clazz);
    }


    /**
     * Use this inside subclasses as a convenience method.
     * 
     * @param <T>
     *            class of DAO to be returned
     * @param clazz
     *            class instance of DAO to be returned
     * @param criterion
     *            criteria for the search
     * @return list of DAOs
     */
    @SuppressWarnings("unchecked")
    protected static <T extends ActiveRecord> List<T> findByCriteria(Class<T> clazz, Criterion... criterion) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Criteria crit = session.createCriteria(clazz);
        for (Criterion c : criterion) {
            crit.add(c);
        }
        return crit.list();
    }


    /**
     * Persist the object in the database.
     */
    public void save() {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.saveOrUpdate(this);
    }


    /**
     * Delete the object from the database.
     */
    public void delete() {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.delete(this);
    }

}
