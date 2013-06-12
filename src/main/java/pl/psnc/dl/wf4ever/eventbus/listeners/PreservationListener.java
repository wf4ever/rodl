package pl.psnc.dl.wf4ever.eventbus.listeners;

import java.io.IOException;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.darceo.client.DArceoClient;
import pl.psnc.dl.wf4ever.darceo.client.DArceoException;
import pl.psnc.dl.wf4ever.eventbus.events.ROAfterCreateEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROAfterDeleteEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROAfterUpdateEvent;
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
public class PreservationListener {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(PreservationListener.class);


    /**
     * Constructor.
     * 
     * @param eventBus
     *            EventBus instance
     */
    public PreservationListener(EventBus eventBus) {
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

        try {
            DArceoClient.getInstance().update(event.getResearchObjectComponent().getResearchObject());
        } catch (DArceoException | IOException e) {
            LOGGER.error("Can't update dArceo " + event.getResearchObjectComponent().getResearchObject().getUri(), e);
        }

    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterROComponentDelete(ROComponentAfterDeleteEvent event) {
        try {
            DArceoClient.getInstance().update(event.getResearchObjectComponent().getResearchObject());
        } catch (DArceoException | IOException e) {
            LOGGER.error("Can't update dArceo " + event.getResearchObjectComponent().getResearchObject().getUri(), e);
        }
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterROComponentUpdate(ROComponentAfterUpdateEvent event) {
        try {
            DArceoClient.getInstance().update(event.getResearchObjectComponent().getResearchObject());
        } catch (DArceoException | IOException e) {
            LOGGER.error("Can't update dArceo " + event.getResearchObjectComponent().getResearchObject().getUri(), e);
        }
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterROCreate(ROAfterCreateEvent event) {
        try {
            DArceoClient.getInstance().post(event.getResearchObject());
        } catch (DArceoException | IOException e) {
            LOGGER.error("Can't store in dArceo " + event.getResearchObject().getUri(), e);
        }

    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterRODelete(ROAfterDeleteEvent event) {
        try {
            DArceoClient.getInstance().delete(event.getResearchObject().getUri());
        } catch (DArceoException | IOException e) {
            LOGGER.error("Can't delete from dArceo " + event.getResearchObject().getUri(), e);
        }
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterUpdate(ROAfterUpdateEvent event) {
        //nth
    }
}
