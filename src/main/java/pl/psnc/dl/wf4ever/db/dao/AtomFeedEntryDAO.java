package pl.psnc.dl.wf4ever.db.dao;

import java.net.URI;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
import pl.psnc.dl.wf4ever.notifications.Notification;

/**
 * Atom Feed Entry DAO.
 * 
 * @author pejot
 * 
 */
public class AtomFeedEntryDAO extends AbstractDAO<Notification> {

    /** Serialization. */
    private static final long serialVersionUID = 1L;

    /** Logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(AtomFeedEntryDAO.class);


    /**
     * Find list of requested Feed Atom Entries.
     * 
     * @param subjectUri
     *            the uri of the entry subject (if null no limits)
     * @param from
     *            the oldest requested date (if null no limits)
     * @param to
     *            the freshest requested date (if null no limits))
     * @param source
     *            entry source
     * @param limit
     *            max number of entries in the feed
     * @return list of requested entries
     */
    @SuppressWarnings("unchecked")
    public List<Notification> find(URI subjectUri, Date from, Date to, URI source, Integer limit) {
        Criteria criteria = HibernateUtil.getSessionFactory().getCurrentSession().createCriteria(Notification.class);
        criteria.addOrder(Order.asc("created"));
        criteria.addOrder(Order.asc("id"));
        if (subjectUri != null) {
            criteria.add(Restrictions.eq("subject", subjectUri.toString()));
        }
        if (from != null) {
            criteria.add(Restrictions.ge("created", from));
        }
        if (to != null) {
            criteria.add(Restrictions.le("created", to));
        }
        if (source != null) {
            criteria.add(Restrictions.eq("source", source.toString()));
        }
        if (limit != null && limit > 0) {
            criteria.setMaxResults(limit);
        }

        return criteria.list();
    }


    /**
     * Check if the given notification already exists to avoid duplications.
     * 
     * @param notification
     *            given notification
     * @return true if exists, false otherwise.
     */
    public boolean isDuplicated(Notification notification) {
        Criteria criteria = HibernateUtil.getSessionFactory().getCurrentSession().createCriteria(Notification.class);
        criteria.add(Restrictions.eq("source", notification.getSource().toString()));
        criteria.add(Restrictions.eq("sourceId", notification.getSourceId()));
        return (criteria.list().size() > 0);
    }


    /**
     * Get all entries.
     * 
     * @return list of requested entries.
     */
    public List<Notification> all() {
        return find(null, null, null, null, null);
    }
}
