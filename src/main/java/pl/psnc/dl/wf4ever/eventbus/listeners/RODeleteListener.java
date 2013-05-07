package pl.psnc.dl.wf4ever.eventbus.listeners;

import pl.psnc.dl.wf4ever.ApplicationProperties;
import pl.psnc.dl.wf4ever.db.dao.AtomFeedEntryDAO;
import pl.psnc.dl.wf4ever.eventbus.events.RODeleteEvent;
import pl.psnc.dl.wf4ever.notifications.Notification;
import pl.psnc.dl.wf4ever.notifications.Notification.Summary;
import pl.psnc.dl.wf4ever.notifications.Notification.Title;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Research Object delete event listener.
 * 
 * @author pejot
 * 
 */
public class RODeleteListener {

    /**
     * Constructor.
     * 
     * @param eventBus
     *            EventBus instance.
     */
    @Inject
    public RODeleteListener(EventBus eventBus) {
        eventBus.register(this);
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onCreate(RODeleteEvent event) {
        AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
        Notification entry = new Notification.Builder(event.getResearchObject()).title(Title.RESEARCH_OBJECT_DELETED)
                .summary(Summary.deleted(event.getResearchObject())).build();
        entry.setSource(ApplicationProperties.getContextPath(), "RODL");
        dao.save(entry);
    }
}
