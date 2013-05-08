package pl.psnc.dl.wf4ever.eventbus.listeners;

import pl.psnc.dl.wf4ever.eventbus.events.ROAfterCreateEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROAfterUpdateEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROBeforeDeleteEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROComponentAfterCreateEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROComponentAfterDeleteEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROComponentAfterUpdateEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Listener for ResearchObject and ResearchObjectComponent, performs operation on solr indexs.
 * 
 * @author pejot
 * 
 */
public class SolrListener {

    /**
     * Constructor.
     * 
     * @param eventBus
     *            EventBus instance
     */
    public SolrListener(EventBus eventBus) {
        eventBus.register(this);
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterROComponentCreate(ROComponentAfterCreateEvent event) {
        event.getResearchObjectComponent().getResearchObject().updateIndexAttributes();
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterROComponentDelete(ROComponentAfterDeleteEvent event) {
        event.getResearchObjectComponent().getResearchObject().updateIndexAttributes();

    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterROComponentUpdate(ROComponentAfterUpdateEvent event) {
        event.getResearchObjectComponent().getResearchObject().updateIndexAttributes();
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterROCreate(ROAfterCreateEvent event) {
        event.getResearchObject().updateIndexAttributes();
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onBeforeRODelete(ROBeforeDeleteEvent event) {
        event.getResearchObject().deleteIndexAttributes();
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterUpdate(ROAfterUpdateEvent event) {
        event.getResearchObject().updateIndexAttributes();
    }
}
