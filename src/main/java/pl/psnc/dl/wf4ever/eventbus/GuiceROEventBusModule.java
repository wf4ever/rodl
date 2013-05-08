package pl.psnc.dl.wf4ever.eventbus;

import pl.psnc.dl.wf4ever.eventbus.listeners.NotificationsListener;
import pl.psnc.dl.wf4ever.eventbus.listeners.SolrListener;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;

/**
 * Configure dependency injection.
 * 
 * @author pejot
 * 
 */
public class GuiceROEventBusModule extends AbstractModule {

    /** EventBus instance. */
    private static EventBus eventBus;
    /** Solr listener instance. */
    private SolrListener solrListener;
    /** Notification listener instance. */
    private NotificationsListener notificationsListener;


    @Override
    protected void configure() {
        eventBus = new EventBus("main-event-bus");
        solrListener = new SolrListener(eventBus);
        notificationsListener = new NotificationsListener(eventBus);
        bind(EventBus.class).toInstance(eventBus);
        bind(SolrListener.class).toInstance(solrListener);
        bind(NotificationsListener.class).toInstance(notificationsListener);

    }
}
