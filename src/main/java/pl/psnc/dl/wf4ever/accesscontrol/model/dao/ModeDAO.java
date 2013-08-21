package pl.psnc.dl.wf4ever.accesscontrol.model.dao;

import pl.psnc.dl.wf4ever.accesscontrol.model.Mode;
import pl.psnc.dl.wf4ever.db.dao.AbstractDAO;

/**
 * Research Object access mode DAO.
 * 
 * @author pejot
 * 
 */
public final class ModeDAO extends AbstractDAO<Mode> {

    /** id. */
    private static final long serialVersionUID = -4468344863067565271L;


    /**
     * Find mode by id.
     * 
     * @param id
     *            uri (id)
     * @return client or null
     */
    public Mode findById(String id) {
        return findByPrimaryKey(Mode.class, id);
    }

}
