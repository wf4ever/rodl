package pl.psnc.dl.wf4ever.eventbus.listeners;

import java.net.URI;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.db.dao.ResearchObjectPreservationStatusDAO;
import pl.psnc.dl.wf4ever.eventbus.events.ROAfterCreateEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROAfterDeleteEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROAfterUpdateEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROComponentAfterCreateEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROComponentAfterDeleteEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROComponentAfterUpdateEvent;
import pl.psnc.dl.wf4ever.preservation.ResearchObjectPreservationStatus;
import pl.psnc.dl.wf4ever.preservation.Status;

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
    /** Research Object Status DAO. */
    private ResearchObjectPreservationStatusDAO dao = new ResearchObjectPreservationStatusDAO();


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
        URI researchObjectUri = event.getResearchObjectComponent().getResearchObject().getUri();
        ResearchObjectPreservationStatus preservationStatus = dao.findById(researchObjectUri.toString());
        if (preservationStatus != null && preservationStatus.getStatus() != null
                && preservationStatus.getStatus() == Status.UP_TO_DATE) {
            preservationStatus.setStatus(Status.UPDATED);
            dao.save(preservationStatus);
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
        URI researchObjectUri = event.getResearchObjectComponent().getResearchObject().getUri();
        ResearchObjectPreservationStatus preservationStatus = dao.findById(researchObjectUri.toString());
        if (preservationStatus.getStatus() != null && preservationStatus.getStatus() == Status.UP_TO_DATE) {
            preservationStatus.setStatus(Status.UPDATED);
            dao.save(preservationStatus);
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
        URI researchObjectUri = event.getResearchObjectComponent().getResearchObject().getUri();
        ResearchObjectPreservationStatus preservationStatus = dao.findById(researchObjectUri.toString());
        if (preservationStatus.getStatus() != null && preservationStatus.getStatus() == Status.UP_TO_DATE) {
            preservationStatus.setStatus(Status.UPDATED);
            dao.save(preservationStatus);
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
        URI researchObjectUri = event.getResearchObject().getUri();
        dao.save(new ResearchObjectPreservationStatus(researchObjectUri, Status.NEW));
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterRODelete(ROAfterDeleteEvent event) {
        URI researchObjectUri = event.getResearchObject().getUri();
        ResearchObjectPreservationStatus preservationStatus = dao.findById(researchObjectUri.toString());
        preservationStatus.setStatus(Status.DELETED);
        dao.save(preservationStatus);
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
