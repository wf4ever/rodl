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
 * @author Piotr Hołubowicz
 * 
 */
public class OAuthManager {

    private static final Logger log = Logger.getLogger(OAuthManager.class);

    private static final int TOKEN_LENGTH = 20;

    private static final SessionFactory sessionFactory;

    static {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            log.error("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }


    public UserCredentials createUserCredentials(String userId, String password) {
        if (userId == null || userId.isEmpty())
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        if (password == null)
            throw new IllegalArgumentException("Password cannot be null.");

        UserCredentials creds = new UserCredentials(userId, password);

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        session.save(creds);

        session.getTransaction().commit();
        return creds;
    }


    public AccessToken createAccessToken(String clientId, String userId) {
        if (clientId == null || clientId.isEmpty())
            throw new IllegalArgumentException("Client id cannot be null or empty.");
        if (userId == null || userId.isEmpty())
            throw new IllegalArgumentException("User cannot be null or empty.");
        UserCredentials creds = getUserCredentials(userId);
        if (creds == null)
            throw new IllegalArgumentException("User not found");
        OAuthClient client = getClient(clientId);
        if (client == null)
            throw new IllegalArgumentException("Client not found");

        String token = generateRandomToken();
        AccessToken at = new AccessToken(token, client, creds);
        at.setCreated(new Date());

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        session.save(at);

        session.getTransaction().commit();
        return at;
    }


    private UserCredentials getUserCredentials(String userId) {
        if (userId == null || userId.isEmpty())
            throw new IllegalArgumentException("User ID cannot be null or empty.");

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        UserCredentials user = (UserCredentials) session.get(UserCredentials.class, userId);

        session.getTransaction().commit();

        return user;
    }


    private String generateRandomToken() {
        return UUID.randomUUID().toString().substring(0, TOKEN_LENGTH);
    }


    public void deleteUserCredentials(String userId) {
        if (userId == null || userId.isEmpty())
            throw new IllegalArgumentException("User ID cannot be null or empty.");

        UserCredentials creds = getUserCredentials(userId);

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        session.delete(creds);
        session.getTransaction().commit();
    }


    public void deleteToken(String token) {
        if (token == null || token.isEmpty())
            throw new IllegalArgumentException("Token cannot be null or empty.");

        AccessToken at = getAccessToken(token);

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        session.delete(at);
        session.getTransaction().commit();
    }


    public List<AccessToken> getAccessTokens(String clientId, String userId) {
        Session session = sessionFactory.getCurrentSession();
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


    public AccessToken getAccessToken(String token) {
        if (token == null || token.isEmpty())
            throw new IllegalArgumentException("Token cannot be null or empty.");

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        AccessToken at = (AccessToken) session.get(AccessToken.class, token);

        if (at != null) {
            at.setLastUsed(new Date());
            session.save(at);
        }

        session.getTransaction().commit();

        return at;
    }


    public String createClient(String name, String redirectionURI) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        String clientId = generateRandomToken();
        session.saveOrUpdate(new OAuthClient(clientId, name, redirectionURI));

        session.getTransaction().commit();

        return clientId;
    }


    public void deleteClient(String clientId) {
        if (clientId == null || clientId.isEmpty())
            throw new IllegalArgumentException("Client id cannot be null or empty.");

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        Query query = session.createQuery("delete OAuthClient where clientId = :clientId");
        query.setParameter("clientId", clientId);
        int result = query.executeUpdate();

        if (result != 1) {
            log.warn(String.format("Deleted %d rows when deleting %s", result, clientId));
        }

        session.getTransaction().commit();
    }


    public OAuthClient getClient(String clientId) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        OAuthClient client = (OAuthClient) session.get(OAuthClient.class, clientId);

        session.getTransaction().commit();

        return client;
    }


    public List<OAuthClient> getClients() {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        Query query = session.createQuery("from OAuthClient");
        @SuppressWarnings("unchecked")
        List<OAuthClient> list = query.list();

        session.getTransaction().commit();

        return list;
    }
}
