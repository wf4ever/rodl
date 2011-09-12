/**
 * 
 */
package pl.psnc.dl.wf4ever.auth;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
{

	private String token;

	private String clientId;

	private String user;


	public AccessToken()
	{

	}


	public AccessToken(String token, String clientId, String user)
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
	@Basic
	@Column(name = "username")
	@XmlElement
	public String getUser()
	{
		return user;
	}


	/**
	 * @param user the user to set
	 */
	public void setUser(String user)
	{
		this.user = user;
	}

}
