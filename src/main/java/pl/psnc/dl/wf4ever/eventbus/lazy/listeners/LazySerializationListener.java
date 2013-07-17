package pl.psnc.dl.wf4ever.eventbus.lazy.listeners;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.eventbus.events.ScheduleToSerializationEvent;
import pl.psnc.dl.wf4ever.model.RDF.Thing;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Listener for ResearchObject and ResearchObjectComponent, performs operation on solr indexs.
 * 
 * @author pejot
 * 
 */
public class LazySerializationListener {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(LazySerializationListener.class);

    /** Serialization requests. */
    private Map<Thing, ScheduleToSerializationEvent> requests = new HashMap<>();


    /**
     * Constructor.
     * 
     * @param eventBus
     *            EventBus instance
     */
    public LazySerializationListener(EventBus eventBus) {
        eventBus.register(this);
    }


    /**
     * Schedule a serialization action. Replace a previous one for the same resource, if existed.
     * 
     * @param event
     *            event with request details
     */
    @Subscribe
    public void onSchedule(ScheduleToSerializationEvent event) {
        requests.put(event.getThing(), event);
    }


    /**
     * Reindex all necessary ROs.
     */
    public void commit() {
        for (ScheduleToSerializationEvent event : requests.values()) {
            try {
                event.getThing().serialize(event.getBase(), event.getFormat());
            } catch (Exception e) {
                LOGGER.error("Could not serialize resource " + event.getThing(), e);
            }
        }
    }
}
