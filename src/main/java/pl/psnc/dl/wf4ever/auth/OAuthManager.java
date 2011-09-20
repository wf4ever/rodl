/**
 * 
 */
package pl.psnc.dl.wf4ever.auth;

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
public class OAuthManager
{

	private static final Logger log = Logger.getLogger(OAuthManager.class);

	private static final int TOKEN_LENGTH = 20;

	private final SessionFactory sessionFactory;


	public OAuthManager()
	{
		try {
			// Create the SessionFactory from hibernate.cfg.xml
			this.sessionFactory = new Configuration().configure()
					.buildSessionFactory();
		}
		catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			log.error("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}


	public UserCredentials createUserCredentials(String username,
			String password)
	{
		if (username == null || username.isEmpty())
			throw new IllegalArgumentException(
					"Username cannot be null or empty.");
		if (password == null)
			throw new IllegalArgumentException("Password cannot be null.");

		UserCredentials creds = new UserCredentials(username, password);

		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		session.save(creds);

		session.getTransaction().commit();
		return creds;
	}


	public AccessToken createAccessToken(String clientId, String username)
	{
		if (clientId == null || clientId.isEmpty())
			throw new IllegalArgumentException(
					"Client id cannot be null or empty.");
		if (username == null || username.isEmpty())
			throw new IllegalArgumentException("User cannot be null or empty.");
		UserCredentials creds = getUserCredentials(username);
		if (creds == null)
			throw new IllegalArgumentException("User not found");
		OAuthClient client = getClient(clientId);
		if (client == null)
			throw new IllegalArgumentException("Client not found");

		String token = generateRandomToken();
		AccessToken at = new AccessToken(token, client, creds);

		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		session.save(at);

		session.getTransaction().commit();
		return at;
	}


	private UserCredentials getUserCredentials(String username)
	{
		if (username == null || username.isEmpty())
			throw new IllegalArgumentException(
					"Username cannot be null or empty.");

		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		UserCredentials user = (UserCredentials) session.get(
			UserCredentials.class, username);

		session.getTransaction().commit();

		return user;
	}


	private String generateRandomToken()
	{
		return UUID.randomUUID().toString().substring(0, TOKEN_LENGTH);
	}


	public void deleteUserCredentials(String username)
	{
		if (username == null || username.isEmpty())
			throw new IllegalArgumentException(
					"Username cannot be null or empty.");

		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		Query query = session
				.createQuery("delete UserCredentials where username = :username");
		query.setParameter("username", username);
		int result = query.executeUpdate();

		if (result != 1) {
			log.warn(String.format("Deleted %d rows when deleting %s", result,
				username));
		}

		session.getTransaction().commit();
	}


	public void deleteToken(String token)
	{
		if (token == null || token.isEmpty())
			throw new IllegalArgumentException("Token cannot be null or empty.");

		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		Query query = session
				.createQuery("delete AccessToken where token = :token");
		query.setParameter("token", token);
		int result = query.executeUpdate();

		if (result != 1) {
			log.warn(String.format("Deleted %d rows when deleting %s", result,
				token));
		}

		session.getTransaction().commit();
	}


	public List<AccessToken> getAccessTokens(String clientId, String username)
	{
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		Query query;
		if (clientId != null && username != null) {
			query = session
					.createQuery("from AccessToken where clientId = :clientId and username = :username");
			query.setParameter("clientId", clientId);
			query.setParameter("user", username);
		}
		else if (clientId != null) {
			query = session
					.createQuery("from AccessToken where clientId = :clientId");
			query.setParameter("clientId", clientId);
		}
		else if (username != null) {
			query = session.createQuery("from AccessToken where user = :user");
			query.setParameter("user", username);
		}
		else {
			query = session.createQuery("from AccessToken");
		}
		@SuppressWarnings("unchecked")
		List<AccessToken> list = query.list();

		session.getTransaction().commit();

		return list;
	}


	public AccessToken getAccessToken(String token)
	{
		if (token == null || token.isEmpty())
			throw new IllegalArgumentException("Token cannot be null or empty.");

		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		AccessToken at = (AccessToken) session.get(AccessToken.class, token);

		session.getTransaction().commit();

		return at;
	}


	public boolean isValid(String token, String user)
	{
		AccessToken at = getAccessToken(token);
		return at != null && at.getUser().equals(user);
	}


	public boolean clientExists(String clientId)
	{
		return getClient(clientId) != null;
	}


	public void storeClient(OAuthClient client)
	{
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		session.saveOrUpdate(client);

		session.getTransaction().commit();
	}


	public void deleteClient(String clientId)
	{
		if (clientId == null || clientId.isEmpty())
			throw new IllegalArgumentException("Client id cannot be null or empty.");

		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		Query query = session
				.createQuery("delete OAuthClient where clientId = :clientId");
		query.setParameter("clientId", clientId);
		int result = query.executeUpdate();

		if (result != 1) {
			log.warn(String.format("Deleted %d rows when deleting %s", result,
				clientId));
		}

		session.getTransaction().commit();
	}


	public OAuthClient getClient(String clientId)
	{
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		OAuthClient client = (OAuthClient) session.get(OAuthClient.class,
			clientId);

		session.getTransaction().commit();

		return client;
	}


	public List<OAuthClient> getClients()
	{
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		Query query = session.createQuery("from OAuthClient");
		@SuppressWarnings("unchecked")
		List<OAuthClient> list = query.list();

		session.getTransaction().commit();

		return list;
	}
}
