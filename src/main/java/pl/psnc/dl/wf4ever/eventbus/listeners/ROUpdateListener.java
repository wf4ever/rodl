package pl.psnc.dl.wf4ever.eventbus.listeners;

import pl.psnc.dl.wf4ever.eventbus.events.ROAfterUpdateEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Research Object create event listener.
 * 
 * @author pejot
 * 
 */
public class ROUpdateListener {

    /**
     * Constructor.
     * 
     * @param eventBus
     *            EventBus instance.
     */
    @Inject
    public ROUpdateListener(EventBus eventBus) {
        eventBus.register(this);
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterUpdate(ROAfterUpdateEvent event) {
        event.getResearchObject().updateIndexAttributes();
    }
}
