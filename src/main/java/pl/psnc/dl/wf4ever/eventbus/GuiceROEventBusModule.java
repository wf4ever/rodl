package pl.psnc.dl.wf4ever.eventbus;

import pl.psnc.dl.wf4ever.eventbus.listeners.ROComponentCreateListener;
import pl.psnc.dl.wf4ever.eventbus.listeners.ROComponentDeleteListener;
import pl.psnc.dl.wf4ever.eventbus.listeners.ROComponentUpdateListener;
import pl.psnc.dl.wf4ever.eventbus.listeners.ROCreateListener;
import pl.psnc.dl.wf4ever.eventbus.listeners.RODeleteListener;
import pl.psnc.dl.wf4ever.eventbus.listeners.ROUpdateListener;

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
    /** SimpleListener instance. */
    private static ROCreateListener roCreateListener;
    /** SimpleListener instance. */
    private static RODeleteListener roDeleteListener;
    /** SimpleListener instance. */
    private static ROUpdateListener roUpdateListener;
    /** SimpleListener instance. */
    private static ROComponentCreateListener roComponentCreateListener;
    /** SimpleListener instance. */
    private static ROComponentDeleteListener roComponentDeleteListener;
    /** SimpleListener instance. */
    private static ROComponentUpdateListener roComponentUpdateListener;


    @Override
    protected void configure() {
        eventBus = new EventBus("main-event-bus");
        roCreateListener = new ROCreateListener(eventBus);
        roUpdateListener = new ROUpdateListener(eventBus);
        roDeleteListener = new RODeleteListener(eventBus);
        roComponentCreateListener = new ROComponentCreateListener(eventBus);
        roComponentUpdateListener = new ROComponentUpdateListener(eventBus);
        roComponentDeleteListener = new ROComponentDeleteListener(eventBus);
        bind(EventBus.class).toInstance(eventBus);
        bind(ROCreateListener.class).toInstance(roCreateListener);
        bind(RODeleteListener.class).toInstance(roDeleteListener);
        bind(ROUpdateListener.class).toInstance(roUpdateListener);
        bind(ROComponentCreateListener.class).toInstance(roComponentCreateListener);
        bind(ROComponentDeleteListener.class).toInstance(roComponentDeleteListener);
        bind(ROComponentUpdateListener.class).toInstance(roComponentUpdateListener);
    }
}
