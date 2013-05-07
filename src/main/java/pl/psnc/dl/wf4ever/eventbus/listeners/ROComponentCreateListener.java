package pl.psnc.dl.wf4ever.eventbus.listeners;

import pl.psnc.dl.wf4ever.eventbus.events.ROComponentAfterCreateEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROComponentBeforeCreateEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Research Object create event listener.
 * 
 * @author pejot
 * 
 */
public class ROComponentCreateListener {

    /**
     * Constructor.
     * 
     * @param eventBus
     *            EventBus instance.
     */
    @Inject
    public ROComponentCreateListener(EventBus eventBus) {
        eventBus.register(this);
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onBeforeCreate(ROComponentBeforeCreateEvent event) {
        //nth for now
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterCreate(ROComponentAfterCreateEvent event) {
        event.getResearchObjectComponent().getResearchObject().updateIndexAttributes();

    }
}
