package pl.psnc.dl.wf4ever.auth;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "clients")
public class OAuthClientList
{

	protected List<OAuthClient> list;


	public OAuthClientList()
	{
	}


	public OAuthClientList(List<OAuthClient> list)
	{
		this.list = list;
	}


	@XmlElement(name = "client")
	public List<OAuthClient> getList()
	{
		return list;
	}
}