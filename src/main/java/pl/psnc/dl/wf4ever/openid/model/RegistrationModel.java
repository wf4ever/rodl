package pl.psnc.dl.wf4ever.openid.model;

import java.io.Serializable;

public class RegistrationModel
	implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2834908356868001273L;

	private String openId;

	private String fullName;

	private String firstName;

	private String lastName;

	private String emailAddress;

	private String country;

	private String language;


	public String getOpenId()
	{
		return openId;
	}


	public void setOpenId(String openId)
	{
		this.openId = openId;
	}


	public String getFullName()
	{
		return fullName;
	}


	public void setFullName(String fullname)
	{
		this.fullName = fullname;
	}


	public String getFirstName()
	{
		return firstName;
	}


	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}


	public String getLastName()
	{
		return lastName;
	}


	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}


	public String getEmailAddress()
	{
		return emailAddress;
	}


	public void setEmailAddress(String emailAddress)
	{
		this.emailAddress = emailAddress;
	}


	public String getCountry()
	{
		return country;
	}


	public void setCountry(String country)
	{
		this.country = country;
	}


	public String getLanguage()
	{
		return language;
	}


	public void setLanguage(String language)
	{
		this.language = language;
	}

}
