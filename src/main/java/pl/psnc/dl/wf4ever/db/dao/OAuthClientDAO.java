/**
 * 
 */
package pl.psnc.dl.wf4ever.db.dao;

import java.util.List;

import pl.psnc.dl.wf4ever.db.OAuthClient;

/**
 * RODL user model.
 * 
 * @author piotrhol
 * 
 */
public final class OAuthClientDAO extends AbstractDAO<OAuthClient> {

    /** id. */
    private static final long serialVersionUID = -4468344863067565271L;


    /**
     * Find in database by client id.
     * 
     * @param clientId
     *            client id
     * @return client or null
     */
    public OAuthClient findById(String clientId) {
        return findByPrimaryKey(OAuthClient.class, clientId);
    }


    /**
     * Find all clients.
     * 
     * @return a list of clients
     */
    public List<OAuthClient> findAll() {
        return findAll(OAuthClient.class);
    }
}
