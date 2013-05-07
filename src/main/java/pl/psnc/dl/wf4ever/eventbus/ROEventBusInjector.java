package pl.psnc.dl.wf4ever.eventbus;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * GuiceROEventBusModule Injector Singleton wrapper.
 * 
 * @author pejot
 * 
 */
public final class ROEventBusInjector {

    /** GuiceROEventBusModule instance. */
    static Injector guiceROEventBusInjector = null;


    /**
     * Hidden constructor.
     */
    private ROEventBusInjector() {

    }


    /**
     * Get EventBus instance.
     * 
     * @return EventBus instance.
     */
    public static Injector getInjector() {
        if (guiceROEventBusInjector == null) {
            guiceROEventBusInjector = Guice.createInjector(new GuiceROEventBusModule());
        }
        return guiceROEventBusInjector;

    }
}
