/**
 * 
 */
package pl.psnc.dl.wf4ever.auth;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * A utility class for accessing the database for the OAuth DAOs.
 * 
 * @author Piotr Hołubowicz
 * 
 */
public class OAuthManager {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(OAuthManager.class);

    /** length of generated tokens. */
    private static final int TOKEN_LENGTH = 20;

    /** Hibernate session factory. */
    private static final SessionFactory SESSION_FACTORY;

    static {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            SESSION_FACTORY = new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            LOGGER.error("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }


    /**
     * Create a new user credentials DAO and store it.
     * 
     * @param userId
     *            user id
     * @param password
     *            user dLibra passwords
     * @return the user credentials DAO
     */
    public UserCredentials createUserCredentials(String userId, String password) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null.");
        }

        UserCredentials creds = new UserCredentials(userId, password);

        Session session = SESSION_FACTORY.getCurrentSession();
        session.beginTransaction();

        session.save(creds);

        session.getTransaction().commit();
        return creds;
    }


    /**
     * Create a new access token DAO and store it.
     * 
     * @param clientId
     *            client application id
     * @param userId
     *            user id
     * @return the access token DAO
     */
    public AccessToken createAccessToken(String clientId, String userId) {
        if (clientId == null || clientId.isEmpty()) {
            throw new IllegalArgumentException("Client id cannot be null or empty.");
        }
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User cannot be null or empty.");
        }
        UserCredentials creds = getUserCredentials(userId);
        if (creds == null) {
            throw new IllegalArgumentException("User not found");
        }
        OAuthClient client = getClient(clientId);
        if (client == null) {
            throw new IllegalArgumentException("Client not found");
        }

        String token = generateRandomToken();
        AccessToken at = new AccessToken(token, client, creds);
        at.setCreated(new Date());

        Session session = SESSION_FACTORY.getCurrentSession();
        session.beginTransaction();

        session.save(at);

        session.getTransaction().commit();
        return at;
    }


    /**
     * Get user credentials.
     * 
     * @param userId
     *            user id
     * @return user id and password
     */
    private UserCredentials getUserCredentials(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }

        Session session = SESSION_FACTORY.getCurrentSession();
        session.beginTransaction();

        UserCredentials user = (UserCredentials) session.get(UserCredentials.class, userId);

        session.getTransaction().commit();

        return user;
    }


    /**
     * Generate an access token.
     * 
     * @return a random string of chars of the length of TOKEN_LENGTH
     */
    private String generateRandomToken() {
        return UUID.randomUUID().toString().substring(0, TOKEN_LENGTH);
    }


    /**
     * Delete user credentials.
     * 
     * @param userId
     *            user id
     */
    public void deleteUserCredentials(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }

        UserCredentials creds = getUserCredentials(userId);

        Session session = SESSION_FACTORY.getCurrentSession();
        session.beginTransaction();
        session.delete(creds);
        session.getTransaction().commit();
    }


    /**
     * Delete an access token.
     * 
     * @param token
     *            access token
     */
    public void deleteToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty.");
        }

        AccessToken at = getAccessToken(token);

        Session session = SESSION_FACTORY.getCurrentSession();
        session.beginTransaction();
        session.delete(at);
        session.getTransaction().commit();
    }


    /**
     * Get all access tokens, optionally filtered by a client application or owner.
     * 
     * @param clientId
     *            client application id or null
     * @param userId
     *            user id or null
     * @return a list of access tokens
     */
    public List<AccessToken> getAccessTokens(String clientId, String userId) {
        Session session = SESSION_FACTORY.getCurrentSession();
        session.beginTransaction();

        Query query;
        if (clientId != null && userId != null) {
            query = session
                    .createQuery("from AccessToken as a where a.client.clientId = :clientId and a.user.userId = :userId");
            query.setParameter("clientId", clientId);
            query.setParameter("userId", userId);
        } else if (clientId != null) {
            query = session.createQuery("from AccessToken as a where a.client.clientId = :clientId");
            query.setParameter("clientId", clientId);
        } else if (userId != null) {
            query = session.createQuery("from AccessToken as a where a.user.userId = :userId");
            query.setParameter("userId", userId);
        } else {
            query = session.createQuery("from AccessToken");
        }
        @SuppressWarnings("unchecked")
        List<AccessToken> list = query.list();

        session.getTransaction().commit();

        return list;
    }


    /**
     * Get an access token DAO.
     * 
     * @param token
     *            the access token
     * @return the access token DAO
     */
    public AccessToken getAccessToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty.");
        }

        Session session = SESSION_FACTORY.getCurrentSession();
        session.beginTransaction();

        AccessToken at = (AccessToken) session.get(AccessToken.class, token);

        if (at != null) {
            at.setLastUsed(new Date());
            session.save(at);
        }

        session.getTransaction().commit();

        return at;
    }


    /**
     * Register a new client application.
     * 
     * @param name
     *            client application human friendly name
     * @param redirectionURI
     *            redirection URI for web clients
     * @return client id
     */
    public String createClient(String name, String redirectionURI) {
        Session session = SESSION_FACTORY.getCurrentSession();
        session.beginTransaction();

        String clientId = generateRandomToken();
        session.saveOrUpdate(new OAuthClient(clientId, name, redirectionURI));

        session.getTransaction().commit();

        return clientId;
    }


    /**
     * Delete a client application.
     * 
     * @param clientId
     *            client id
     */
    public void deleteClient(String clientId) {
        if (clientId == null || clientId.isEmpty()) {
            throw new IllegalArgumentException("Client id cannot be null or empty.");
        }

        Session session = SESSION_FACTORY.getCurrentSession();
        session.beginTransaction();

        Query query = session.createQuery("delete OAuthClient where clientId = :clientId");
        query.setParameter("clientId", clientId);
        int result = query.executeUpdate();

        if (result != 1) {
            LOGGER.warn(String.format("Deleted %d rows when deleting %s", result, clientId));
        }

        session.getTransaction().commit();
    }


    /**
     * Read a client application DAO.
     * 
     * @param clientId
     *            client id
     * @return client application DAO
     */
    public OAuthClient getClient(String clientId) {
        Session session = SESSION_FACTORY.getCurrentSession();
        session.beginTransaction();

        OAuthClient client = (OAuthClient) session.get(OAuthClient.class, clientId);

        session.getTransaction().commit();

        return client;
    }


    /**
     * Return a list of all client applications.
     * 
     * @return a list of client applications
     */
    public List<OAuthClient> getClients() {
        Session session = SESSION_FACTORY.getCurrentSession();
        session.beginTransaction();

        Query query = session.createQuery("from OAuthClient");
        @SuppressWarnings("unchecked")
        List<OAuthClient> list = query.list();

        session.getTransaction().commit();

        return list;
    }
}
