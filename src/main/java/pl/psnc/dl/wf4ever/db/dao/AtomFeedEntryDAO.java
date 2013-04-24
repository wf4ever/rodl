package pl.psnc.dl.wf4ever.db.dao;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import pl.psnc.dl.wf4ever.db.AtomFeedEntry;
import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
import pl.psnc.dl.wf4ever.vocabulary.ORE;

import com.hp.hpl.jena.vocabulary.DCTerms;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;

/**
 * Atom Feed entry model.
 * 
 * @author pejot
 * 
 */
public class AtomFeedEntryDAO extends AbstractDAO<AtomFeedEntry> {

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
    public List<AtomFeedEntry> find(URI subjectUri, Date from, Date to, URI source, Integer limit) {
        Criteria criteria = HibernateUtil.getSessionFactory().getCurrentSession().createCriteria(AtomFeedEntry.class);
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
            criteria.add(Restrictions.eq("subject", source.toString()));
        }
        if (limit != null && limit > 0) {
            criteria.setMaxResults(limit);
        }

        return criteria.list();
    }


    /**
     * Get all entries.
     * 
     * @return list of requested entries.
     */
    public List<AtomFeedEntry> all() {
        return find(null, null, null, null, null);
    }


    /**
     * Convert list of records (AtomFeedEntry) to the list of Entry object (from rome package).
     * 
     * @param dbEntries
     *            db records
     * @return list of romes entry
     */
    public static List<Entry> convertToRawEntry(List<AtomFeedEntry> dbEntries) {
        List<Entry> result = new ArrayList<>();
        for (AtomFeedEntry dbEntry : dbEntries) {
            Entry resultEntry = new Entry();
            resultEntry.setId("urn:X-rodl:" + dbEntry.getId().toString());
            if (dbEntry.getTitle() != null) {
                resultEntry.setTitle(dbEntry.getTitle());
            }
            resultEntry.setPublished(dbEntry.getCreated());
            //set summary
            Content content = new Content();
            if (dbEntry.getSummary() != null) {
                content.setValue(dbEntry.getSummary());
                content.setValue(StringEscapeUtils.escapeHtml4(dbEntry.getSummary()));
            }
            resultEntry.setSummary(content);
            //set links
            List<Link> links = new ArrayList<>();
            if (dbEntry.getSource() != null) {
                Link sourceLink = new Link();
                sourceLink.setHref(dbEntry.getSource().toString());
                sourceLink.setRel(DCTerms.source.toString());
                sourceLink.setTitle("Action source/service");
                links.add(sourceLink);
            }
            if (dbEntry.getSubject() != null) {
                Link sourceLink = new Link();
                sourceLink.setHref(dbEntry.getSubject().toString());
                sourceLink.setRel(ORE.describes.toString());
                sourceLink.setTitle("Description for");
                links.add(sourceLink);
            }

            resultEntry.setOtherLinks(links);
            result.add(resultEntry);
        }
        return result;
    }
}
