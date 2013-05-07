package pl.psnc.dl.wf4ever.eventbus.listeners;

import pl.psnc.dl.wf4ever.eventbus.events.ROComponentAfterDeleteEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROComponentBeforeDeleteEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Research Object create event listener.
 * 
 * @author pejot
 * 
 */
public class ROComponentDeleteListener {

    /**
     * Constructor.
     * 
     * @param eventBus
     *            EventBus instance.
     */
    @Inject
    public ROComponentDeleteListener(EventBus eventBus) {
        eventBus.register(this);
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onBeforeDelete(ROComponentBeforeDeleteEvent event) {
        //nth for now
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterDelete(ROComponentAfterDeleteEvent event) {
        event.getResearchObjectComponent().getResearchObject().updateIndexAttributes();

    }
}
