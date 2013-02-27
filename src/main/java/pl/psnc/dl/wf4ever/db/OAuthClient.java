/**
 * 
 */
package pl.psnc.dl.wf4ever.db;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
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
public class OAuthClient implements Serializable {

    /** id. */
    private static final long serialVersionUID = -2685385714424480380L;

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
     * Constructor. Client ID will be a random UUID.
     * 
     * @param name
     *            client name for humans
     * @param redirectionURI
     *            client redirection URI in case of web clients
     */
    public OAuthClient(String name, String redirectionURI) {
        this.clientId = UUID.randomUUID().toString();
        this.name = name;
        this.redirectionURI = redirectionURI;
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
    @Column(length = 128)
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
