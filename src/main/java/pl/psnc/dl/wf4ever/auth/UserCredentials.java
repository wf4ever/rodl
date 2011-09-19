/**
 * 
 */
package pl.psnc.dl.wf4ever.auth;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
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
	 * @param username
	 * @param password
	 */
	public UserCredentials()
	{
	}


	/**
	 * @param username
	 * @param password
	 */
	public UserCredentials(String username, String password)
	{
		this.username = username;
		this.password = password;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7967488547563569388L;

	private String username;

	private String password;


	/**
	 * @return the user
	 */
	@Id
	@XmlElement
	public String getUsername()
	{
		return username;
	}


	/**
	 * @param user the user to set
	 */
	public void setUsername(String user)
	{
		this.username = user;
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

}
