/**
 * 
 */
package pl.psnc.dl.wf4ever.auth;

import javax.persistence.Basic;
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
@Table(name = "clients")
@XmlRootElement(name = "client")
public class OAuthClient
{

	private String clientId;

	private String name;

	private String redirectionURI;


	public OAuthClient()
	{

	}


	/**
	 * @param clientId
	 * @param name
	 * @param redirectionURI
	 */
	public OAuthClient(String clientId, String name, String redirectionURI)
	{
		this.clientId = clientId;
		this.name = name;
		this.redirectionURI = redirectionURI;
	}


	/**
	 * @return the clientId
	 */
	@Id
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
	 * @return the name
	 */
	@Basic
	@XmlElement
	public String getName()
	{
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}


	/**
	 * @return the redirectionURI
	 */
	@Basic
	@XmlElement
	public String getRedirectionURI()
	{
		return redirectionURI;
	}


	/**
	 * @param redirectionURI the redirectionURI to set
	 */
	public void setRedirectionURI(String redirectionURI)
	{
		this.redirectionURI = redirectionURI;
	}
}
