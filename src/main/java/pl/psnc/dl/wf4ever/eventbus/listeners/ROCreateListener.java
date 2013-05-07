package pl.psnc.dl.wf4ever.eventbus.listeners;

import pl.psnc.dl.wf4ever.ApplicationProperties;
import pl.psnc.dl.wf4ever.db.dao.AtomFeedEntryDAO;
import pl.psnc.dl.wf4ever.eventbus.events.ROAfterCreateEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROBeforeCreateEvent;
import pl.psnc.dl.wf4ever.notifications.Notification;
import pl.psnc.dl.wf4ever.notifications.Notification.Summary;
import pl.psnc.dl.wf4ever.notifications.Notification.Title;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Research Object create event listener.
 * 
 * @author pejot
 * 
 */
public class ROCreateListener {

    /**
     * Constructor.
     * 
     * @param eventBus
     *            EventBus instance.
     */
    @Inject
    public ROCreateListener(EventBus eventBus) {
        eventBus.register(this);
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onBeforeCreate(ROBeforeCreateEvent event) {
        //nth for now
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterCreate(ROAfterCreateEvent event) {
        AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
        Notification entry = new Notification.Builder(event.getResearchObject().getUri())
                .title(Title.RESEARCH_OBJECT_CREATED).summary(Summary.created(event.getResearchObject())).build();
        entry.setSource(ApplicationProperties.getContextPath(), "RODL");
        dao.save(entry);
        event.getResearchObject().updateIndexAttributes();
    }
}
