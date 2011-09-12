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
public class AccessTokenManager
{

	private static final Logger log = Logger
			.getLogger(AccessTokenManager.class);

	private static final int TOKEN_LENGTH = 20;

	private final SessionFactory sessionFactory;


	public AccessTokenManager()
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


	public AccessToken createAccessToken(String clientId, String user)
	{
		if (clientId == null || clientId.isEmpty())
			throw new IllegalArgumentException(
					"Client id cannot be null or empty.");
		if (user == null || user.isEmpty())
			throw new IllegalArgumentException("User cannot be null or empty.");
		String token = generateRandomToken();
		AccessToken at = new AccessToken(token, clientId, user);

		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		session.save(at);

		session.getTransaction().commit();
		return at;
	}


	private String generateRandomToken()
	{
		return UUID.randomUUID().toString().substring(0, TOKEN_LENGTH);
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


	public List<AccessToken> getAccessTokens(String clientId, String user)
	{
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		Query query;
		if (clientId != null && user != null) {
			query = session
					.createQuery("from AccessToken where clientId = :clientId and user = :user");
			query.setParameter("clientId", clientId);
			query.setParameter("user", user);
		}
		else if (clientId != null) {
			query = session
					.createQuery("from AccessToken where clientId = :clientId");
			query.setParameter("clientId", clientId);
		}
		else if (user != null) {
			query = session.createQuery("from AccessToken where user = :user");
			query.setParameter("user", user);
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

		Query query = session
				.createQuery("from AccessToken where token = :token");
		query.setParameter("token", token);
		@SuppressWarnings("unchecked")
		List<AccessToken> list = query.list();

		session.getTransaction().commit();

		return list.isEmpty() ? null : list.get(0);

	}


	public boolean isValid(String token, String user)
	{
		AccessToken at = getAccessToken(token);
		return at != null && at.getUser().equals(user);
	}

}
