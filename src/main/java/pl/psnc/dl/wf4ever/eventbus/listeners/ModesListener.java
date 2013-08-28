package pl.psnc.dl.wf4ever.eventbus.listeners;

import java.net.URI;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.accesscontrol.model.AccessMode;
import pl.psnc.dl.wf4ever.accesscontrol.model.dao.ModeDAO;
import pl.psnc.dl.wf4ever.eventbus.events.ROAfterCreateEvent;
import pl.psnc.dl.wf4ever.eventbus.events.ROAfterDeleteEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Listener for ResearchObject initiate default access control mode for new Research Object.
 * 
 * @author pejot
 * 
 */
public class ModesListener {

    /** Access Control Mode dao. */
    ModeDAO dao;

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(ModesListener.class);


    /**
     * Constructor.
     * 
     * @param eventBus
     *            EventBus instance
     */
    public ModesListener(EventBus eventBus) {
        eventBus.register(this);
        dao = new ModeDAO();
    }


    /**
     * Subscription method.
     * 
     * @param event
     *            processed event
     */
    @Subscribe
    public void onAfterROCreate(ROAfterCreateEvent event) {
        URI roUri = event.getResearchObject().getUri();
        if (dao.findByResearchObject(roUri.toString()) != null) {
            //@TODO this is an error. Think how to handle it.
            LOGGER.error("The Research Object " + roUri.toString() + " has already defined mode");
        } else {
            AccessMode mode = new AccessMode();
            mode.setMode(pl.psnc.dl.wf4ever.accesscontrol.dicts.Mode.PUBLIC);
            mode.setRo(roUri.toString());
            dao.save(mode);
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
        URI roUri = event.getResearchObject().getUri();
        AccessMode mode = dao.findByResearchObject(roUri.toString());
        if (mode != null) {
            dao.delete(mode);
            //@TODO this is an error. Think how to handle it.
        } else {
            LOGGER.error("The Research Object " + roUri.toString() + " doesn't have defined mode");
        }
    }
}
