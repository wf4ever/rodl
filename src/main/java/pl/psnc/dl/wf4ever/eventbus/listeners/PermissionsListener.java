package pl.psnc.dl.wf4ever.eventbus.listeners;

import java.net.URI;
import java.util.List;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.accesscontrol.model.Permission;
import pl.psnc.dl.wf4ever.accesscontrol.model.PermissionLink;
import pl.psnc.dl.wf4ever.accesscontrol.model.dao.PermissionDAO;
import pl.psnc.dl.wf4ever.accesscontrol.model.dao.PermissionLinkDAO;
import pl.psnc.dl.wf4ever.db.dao.UserProfileDAO;
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
public class PermissionsListener {

    /** Access Control Permissions dao. */
    PermissionDAO dao;

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(PermissionsListener.class);


    /**
     * Constructor.
     * 
     * @param eventBus
     *            EventBus instance
     */
    public PermissionsListener(EventBus eventBus) {
        eventBus.register(this);
        dao = new PermissionDAO();
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
        if (dao.findByResearchObject(roUri.toString()) != null && dao.findByResearchObject(roUri.toString()).size() > 0) {
            //@TODO this is an error. Think how to handle it.
            LOGGER.error("The Research Object " + roUri.toString() + " has already defined permissions");
        } else {
            Permission permission = new Permission();
            UserProfileDAO userProfileDAO = new UserProfileDAO();
            if (event.getResearchObject().getCreator() != null
                    && userProfileDAO.findByLogin(event.getResearchObject().getCreator().getLogin()) != null) {
                userProfileDAO.findByLogin(event.getResearchObject().getCreator().getLogin());
                permission.setUser(userProfileDAO.findByLogin(event.getResearchObject().getCreator().getLogin()));
                permission.setRo(roUri.toString());
                permission.setRole(pl.psnc.dl.wf4ever.accesscontrol.dicts.Role.OWNER);
                dao.save(permission);
            } else {
                //@TODO this is an error. Think how to handle it.
                LOGGER.error("The Research Object " + roUri.toString()
                        + " doesn't have a Creator. Can't grant a OWNER role");
            }
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
        List<Permission> permissions = dao.findByResearchObject(roUri.toString());
        if (permissions != null) {
            for (Permission p : permissions) {
                dao.delete(p);
            }
            //@TODO this is an error. Think how to handle it.
        } else {
            LOGGER.error("The Research Object " + roUri.toString() + " doesn't have any permissions");
        }
        //remove also permission links
        PermissionLinkDAO linkDao = new PermissionLinkDAO();
        List<PermissionLink> permissionsLinks = linkDao.findByResearchObject(roUri.toString());
        if (permissionsLinks != null) {
            for (PermissionLink p : permissionsLinks) {
                linkDao.delete(p);
            }
        }
    }
}
