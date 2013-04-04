package pl.psnc.dl.wf4ever.db.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.db.AtomFeedEntry;

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
    private static final Logger LOGGER = Logger.getLogger(AtomFeedEntryDAO.class);


    /**
     * Get all entries.
     * 
     * @return a list of clients
     */
    public List<Entry> findAll() {
        List<Entry> result = new ArrayList<>();
        for (AtomFeedEntry dbEntry : findAll(AtomFeedEntry.class)) {
            try {
                Entry resultEntry = new Entry();
                resultEntry.setId(dbEntry.getId().toString());
                resultEntry.setTitle(dbEntry.getTitle());
                resultEntry.setPublished(dbEntry.getCreated());
                //set summary
                Content content = new Content();
                content.setValue(dbEntry.getSummary());
                content.setValue(StringEscapeUtils.escapeHtml4(dbEntry.getSummary()));
                resultEntry.setSummary(content);
                //set links
                Link sourceLink = new Link();
                sourceLink.setHref(dbEntry.getSource().toString());
                sourceLink.setTitle("entry source");
                resultEntry.setOtherLinks(Collections.singletonList(sourceLink));
                result.add(resultEntry);
            } catch (NullPointerException e) {
                String infoID = dbEntry.getId() != null ? dbEntry.getId().toString() : "Unknown";
                LOGGER.error("AtomFeedEntry " + infoID + " isn't completed", e);
            }
        }
        return result;
    }
}
