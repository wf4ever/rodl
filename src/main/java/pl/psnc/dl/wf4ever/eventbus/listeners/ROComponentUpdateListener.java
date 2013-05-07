package pl.psnc.dl.wf4ever.eventbus.listeners;

import pl.psnc.dl.wf4ever.eventbus.events.ROComponentAfterUpdateEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROComponentBeforeUpdateEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Research Object create event listener.
 * 
 * @author pejot
 * 
 */
public class ROComponentUpdateListener {

    /**
     * Constructor.
     * 
     * @param eventBus
     *            EventBus instance.
     */
    @Inject
    public ROComponentUpdateListener(EventBus eventBus) {
        eventBus.register(this);
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onBeforeUpdate(ROComponentBeforeUpdateEvent event) {
        //nth for now
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterUpdate(ROComponentAfterUpdateEvent event) {
        event.getResearchObjectComponent().getResearchObject().updateIndexAttributes();

    }
}
