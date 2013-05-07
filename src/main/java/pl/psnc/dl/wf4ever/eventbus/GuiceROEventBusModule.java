package pl.psnc.dl.wf4ever.eventbus;

import pl.psnc.dl.wf4ever.eventbus.listeners.ROCreateListener;

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


    @Override
    protected void configure() {
        eventBus = new EventBus("main-event-bus");
        roCreateListener = new ROCreateListener(eventBus);
        bind(EventBus.class).toInstance(eventBus);
        bind(ROCreateListener.class).toInstance(roCreateListener);
    }
}
