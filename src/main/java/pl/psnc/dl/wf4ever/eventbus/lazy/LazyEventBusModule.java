package pl.psnc.dl.wf4ever.eventbus.lazy;

import pl.psnc.dl.wf4ever.eventbus.EventBusModule;
import pl.psnc.dl.wf4ever.eventbus.lazy.listeners.LazySerializationListener;
import pl.psnc.dl.wf4ever.eventbus.lazy.listeners.LazySolrListener;
import pl.psnc.dl.wf4ever.eventbus.listeners.NotificationsListener;
import pl.psnc.dl.wf4ever.eventbus.listeners.PreservationListener;

import com.google.common.eventbus.EventBus;

/**
 * A module that has lazy listeners, i.e you have to call commit() in the end.
 * 
 * @author pejot
 * 
 */
public class LazyEventBusModule implements EventBusModule {

    /** EventBus instance. */
    private EventBus eventBus;

    /** Lazy Solr listener. */
    private LazySolrListener solrListener;

    /** Lazy serialization listener. */
    private LazySerializationListener serializationListener;


    /**
     * Constructor.
     */
    public LazyEventBusModule() {
        eventBus = new EventBus("main-event-bus");
        solrListener = new LazySolrListener(eventBus);
        serializationListener = new LazySerializationListener(eventBus);
        new NotificationsListener(eventBus);
        new PreservationListener(eventBus);
    }


    @Override
    public EventBus getEventBus() {
        return eventBus;
    }


    @Override
    public void commit() {
        serializationListener.commit();
        solrListener.commit();
    }
}
