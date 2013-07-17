package pl.psnc.dl.wf4ever.eventbus;

import pl.psnc.dl.wf4ever.eventbus.listeners.NotificationsListener;
import pl.psnc.dl.wf4ever.eventbus.listeners.PreservationListener;
import pl.psnc.dl.wf4ever.eventbus.listeners.SimpleSerializationListener;
import pl.psnc.dl.wf4ever.eventbus.listeners.SolrListener;

import com.google.common.eventbus.EventBus;

/**
 * Configure dependency injection.
 * 
 * @author pejot
 * 
 */
public class SimpleEventBusModule implements EventBusModule {

    /** EventBus instance. */
    private EventBus eventBus;


    /**
     * Constructor.
     */
    public SimpleEventBusModule() {
        eventBus = new EventBus("main-event-bus");
        new SolrListener(eventBus);
        new NotificationsListener(eventBus);
        new PreservationListener(eventBus);
        new SimpleSerializationListener(eventBus);
    }


    @Override
    public EventBus getEventBus() {
        return eventBus;
    }


    @Override
    public void commit() {
    }
}
