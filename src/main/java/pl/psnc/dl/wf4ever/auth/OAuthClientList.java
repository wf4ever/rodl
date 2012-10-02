package pl.psnc.dl.wf4ever.auth;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A list of OAuth clients.
 * 
 * @author piotrekhol
 * 
 */
@XmlRootElement(name = "clients")
public class OAuthClientList {

    /** the list of clients. */
    protected List<OAuthClient> list;


    /**
     * Constructor.
     */
    public OAuthClientList() {
    }


    /**
     * Constructor.
     * 
     * @param list
     *            the list of clients
     */
    public OAuthClientList(List<OAuthClient> list) {
        this.list = list;
    }


    @XmlElement(name = "client")
    public List<OAuthClient> getList() {
        return list;
    }
}
