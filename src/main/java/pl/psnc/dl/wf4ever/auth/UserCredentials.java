/**
 * 
 */
package pl.psnc.dl.wf4ever.auth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Piotr Hołubowicz
 *
 */
@Entity
@Table(name = "usercredentials")
@XmlRootElement
public class UserCredentials
	implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7967488547563569388L;

	public static final UserCredentials PUBLIC_USER = new UserCredentials(
			"wf4ever_reader", "wf4ever_reader!!!");

	private String userId;

	private String password;

	private List<AccessToken> tokens = new ArrayList<AccessToken>();


	/**
	 * @param userId
	 * @param password
	 */
	public UserCredentials()
	{
	}


	/**
	 * @param userId
	 * @param password
	 */
	public UserCredentials(String userId, String password)
		throws IllegalArgumentException
	{
		if (userId == null)
			throw new IllegalArgumentException("User id cannot be null");
		if (password == null)
			throw new IllegalArgumentException("Password cannot be null");
		this.userId = userId;
		this.password = password;
	}


	/**
	 * @return the user
	 */
	@Id
	@XmlElement
	public String getUserId()
	{
		return userId;
	}


	/**
	 * @param userId the user to set
	 */
	public void setUserId(String userId)
	{
		this.userId = userId;
	}


	/**
	 * @return the password
	 */
	@Basic
	@XmlTransient
	public String getPassword()
	{
		return password;
	}


	/**
	 * @param password the password to set
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}


	/**
	 * @return the tokens
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
	@XmlTransient
	public List<AccessToken> getTokens()
	{
		return tokens;
	}


	/**
	 * @param tokens the tokens to set
	 */
	public void setTokens(List<AccessToken> tokens)
	{
		this.tokens = tokens;
	}

}
