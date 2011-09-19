/**
 * 
 */
package pl.psnc.dl.wf4ever.auth;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
public class AccessToken
	implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8724845005623981779L;

	private String token;

	private String clientId;

	private UserCredentials user;


	public AccessToken()
	{

	}


	public AccessToken(String token, String clientId, UserCredentials user)
	{
		super();
		this.token = token;
		this.clientId = clientId;
		this.user = user;
	}


	/**
	 * @return the token
	 */
	@Id
	@XmlElement
	public String getToken()
	{
		return token;
	}


	/**
	 * @param token the token to set
	 */
	public void setToken(String token)
	{
		this.token = token;
	}


	/**
	 * @return the clientId
	 */
	@Basic
	@XmlElement
	public String getClientId()
	{
		return clientId;
	}


	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId)
	{
		this.clientId = clientId;
	}


	/**
	 * @return the user
	 */
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "username", nullable = false)
	@XmlElement
	public UserCredentials getUser()
	{
		return user;
	}


	/**
	 * @param user the user to set
	 */
	public void setUser(UserCredentials user)
	{
		this.user = user;
	}

}
