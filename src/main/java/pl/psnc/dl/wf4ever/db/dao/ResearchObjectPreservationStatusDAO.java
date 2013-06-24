/**
 * 
 */
package pl.psnc.dl.wf4ever.db.dao;

import java.util.List;

import pl.psnc.dl.wf4ever.preservation.ResearchObjectPreservationStatus;

/**
 * Research Object preservation status DAO.
 * 
 * @author pejot
 * 
 */
public final class ResearchObjectPreservationStatusDAO extends AbstractDAO<ResearchObjectPreservationStatus> {

    /** id. */
    private static final long serialVersionUID = -4468344863067565271L;


    /**
     * Find in database by client id.
     * 
     * @param clientId
     *            client id
     * @return client or null
     */
    public ResearchObjectPreservationStatus findById(String clientId) {
        return findByPrimaryKey(ResearchObjectPreservationStatus.class, clientId);
    }


    /**
     * Find all clients.
     * 
     * @return a list of clients
     */
    public List<ResearchObjectPreservationStatus> findAll() {
        return findAll(ResearchObjectPreservationStatus.class);
    }
}
