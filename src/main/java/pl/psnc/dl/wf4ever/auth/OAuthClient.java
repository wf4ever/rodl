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
 * OAuth client application DAO.
 * 
 * @author Piotr Hołubowicz
 * 
 */
@Entity
@Table(name = "clients")
@XmlRootElement(name = "client")
public class OAuthClient {

    /** client id. */
    private String clientId;

    /** client name for humans. */
    private String name;

    /** client redirection URI in case of web clients. */
    private String redirectionURI;


    /**
     * Constructor.
     */
    public OAuthClient() {

    }


    /**
     * Constructor.
     * 
     * @param clientId
     *            client id
     * @param name
     *            client name for humans
     * @param redirectionURI
     *            client redirection URI in case of web clients
     */
    public OAuthClient(String clientId, String name, String redirectionURI) {
        this.clientId = clientId;
        this.name = name;
        this.redirectionURI = redirectionURI;
    }


    @Id
    @XmlElement
    public String getClientId() {
        return clientId;
    }


    public void setClientId(String clientId) {
        this.clientId = clientId;
    }


    @Basic
    @XmlElement
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    @Basic
    @XmlElement
    public String getRedirectionURI() {
        return redirectionURI;
    }


    public void setRedirectionURI(String redirectionURI) {
        this.redirectionURI = redirectionURI;
    }
}
