package pl.psnc.dl.wf4ever.accesscontrol.model.dao;

import pl.psnc.dl.wf4ever.accesscontrol.model.Permission;
import pl.psnc.dl.wf4ever.db.dao.AbstractDAO;

/**
 * Research Object permission DAO.
 * 
 * @author pejot
 * 
 */
public final class PermissionDAO extends AbstractDAO<Permission> {

    /** id. */
    private static final long serialVersionUID = -4468344863067565271L;


    /**
     * Find permission by id.
     * 
     * @param id
     *            uri (id)
     * @return client or null
     */
    public Permission findById(String id) {
        return findByPrimaryKey(Permission.class, id);
    }

}
