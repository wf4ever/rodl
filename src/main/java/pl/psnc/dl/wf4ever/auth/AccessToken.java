/**
 * 
 */
package pl.psnc.dl.wf4ever.auth;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Piotr Hołubowicz
 * 
 */
@Entity
@Table(name = "tokens")
@XmlRootElement(name = "access-token")
public class AccessToken implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8724845005623981779L;

	private String token;

	private OAuthClient client;

	private UserCredentials user;
	
	private Date created;
	
	private Date lastUsed;

	public AccessToken() {

	}

	public AccessToken(String token, OAuthClient client, UserCredentials user) {
		super();
		this.token = token;
		this.client = client;
		this.user = user;
	}

	/**
	 * @return the token
	 */
	@Id
	@XmlElement
	public String getToken() {
		return token;
	}

	/**
	 * @param token
	 *            the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return the client
	 */
	@ManyToOne
	@JoinColumn(nullable = false)
	@XmlElement
	public OAuthClient getClient() {
		return client;
	}

	/**
	 * @param client
	 *            the client to set
	 */
	public void setClient(OAuthClient client) {
		this.client = client;
	}

	/**
	 * @return the user
	 */
	@ManyToOne
	@JoinColumn(name = "userId", nullable = false)
	@XmlElement
	public UserCredentials getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(UserCredentials user) {
		this.user = user;
	}

	@Basic
	@XmlElement
	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	/**
	 * @return the lastUsed
	 */
	@Basic
	@XmlElement
	public Date getLastUsed() {
		return lastUsed;
	}

	/**
	 * @param lastUsed the lastUsed to set
	 */
	public void setLastUsed(Date lastUsed) {
		this.lastUsed = lastUsed;
	}

}
