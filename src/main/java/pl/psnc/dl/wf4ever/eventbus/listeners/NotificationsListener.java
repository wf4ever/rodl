package pl.psnc.dl.wf4ever.eventbus.listeners;

import pl.psnc.dl.wf4ever.ApplicationProperties;
import pl.psnc.dl.wf4ever.db.dao.AtomFeedEntryDAO;
import pl.psnc.dl.wf4ever.eventbus.events.ROAfterCreateEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROAfterDeleteEvent;
import pl.psnc.dl.wf4ever.notifications.Notification;
import pl.psnc.dl.wf4ever.notifications.Notification.Summary;
import pl.psnc.dl.wf4ever.notifications.Notification.Title;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Listener for ResearchObject and ResearchObjectComponent, performs operation on solr indexs.
 * 
 * @author pejot
 * 
 */
public class NotificationsListener {

    /**
     * Constructor.
     * 
     * @param eventBus
     *            EventBus instance
     */
    public NotificationsListener(EventBus eventBus) {
        eventBus.register(this);
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterROCreate(ROAfterCreateEvent event) {
        AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
        Notification entry = new Notification.Builder(event.getResearchObject().getUri())
                .title(Title.created(event.getResearchObject())).summary(Summary.created(event.getResearchObject()))
                .source(ApplicationProperties.getContextPath() != null ? ApplicationProperties.getContextPath() : "/")
                .sourceName("RODL").build();
        dao.save(entry);
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterRODelete(ROAfterDeleteEvent event) {
        AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
        Notification entry = new Notification.Builder(event.getResearchObject())
                .title(Title.deleted(event.getResearchObject())).summary(Summary.deleted(event.getResearchObject()))
                .source(ApplicationProperties.getContextPath() != null ? ApplicationProperties.getContextPath() : "/")
                .sourceName("RODL").build();
        dao.save(entry);
    }
}
