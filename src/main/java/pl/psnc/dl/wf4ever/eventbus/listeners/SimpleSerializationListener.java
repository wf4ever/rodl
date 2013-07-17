package pl.psnc.dl.wf4ever.eventbus.listeners;

import pl.psnc.dl.wf4ever.eventbus.events.ScheduleToSerializationEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Listener for ResearchObject and ResearchObjectComponent, performs operation on solr indexs.
 * 
 * @author pejot
 * 
 */
public class SimpleSerializationListener {

    /**
     * Constructor.
     * 
     * @param eventBus
     *            EventBus instance
     */
    public SimpleSerializationListener(EventBus eventBus) {
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
        event.getThing().serialize(event.getBase(), event.getFormat());
    }

}
