package pl.psnc.dl.wf4ever.notifications;

import java.net.URI;

import pl.psnc.dl.wf4ever.db.AtomFeedEntry;
import pl.psnc.dl.wf4ever.db.dao.AtomFeedEntryDAO;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

/**
 * Builder for AtomFeedEntry.
 * 
 * @author pejot
 * 
 */
public class EntryBuilder {

    /**
     * Hidden constructor.
     */
    protected EntryBuilder() {
        //nope
    }


    /**
     * Create and save a new AtomFeedEntry.
     * 
     * @param researchObject
     *            Research Object.
     * @param action
     *            the resean of the entry creation.
     * @return created entry.
     */
    public static AtomFeedEntry create(ResearchObject researchObject, ActionType action) {
        AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
        AtomFeedEntry entry = new AtomFeedEntry();
        entry.setTitle("Research Object Created");
        entry.setSubject(researchObject.getUri());
        entry.setSource(URI.create("."));
        entry.setSummary("The new Research Object was created.\nResearch Object uri:"
                + researchObject.getUri().toString() + "\n");
        dao.save(entry);
        return entry;
    }
}
