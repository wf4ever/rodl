package pl.psnc.dl.wf4ever.eventbus;

import com.google.common.eventbus.EventBus;

/**
 * A module contains an event bus and all listeners to it. The module may support the commit method, which should be
 * called when the events should take effect.
 * 
 * @author piotrekhol
 * 
 */
public interface EventBusModule {

    /**
     * Return the only event bus. This instance should be always the same because it is shared for all model objects.
     * 
     * @return the event bus
     */
    EventBus getEventBus();


    /**
     * Apply the events (if not applied already).
     */
    void commit();

}
