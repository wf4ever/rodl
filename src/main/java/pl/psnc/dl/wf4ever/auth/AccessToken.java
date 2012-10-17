/**
 * 
 */
package pl.psnc.dl.wf4ever.auth;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import pl.psnc.dl.wf4ever.common.ActiveRecord;
import pl.psnc.dl.wf4ever.common.HibernateUtil;

/**
 * OAuth access token DAO.
 * 
 * @author Piotr Hołubowicz
 * 
 */
@Entity
@Table(name = "tokens")
@XmlRootElement(name = "access-token")
public class AccessToken extends ActiveRecord {

    /** id. */
    private static final long serialVersionUID = 8724845005623981779L;

    /** token. */
    private String token;

    /** client application. */
    private OAuthClient client;

    /** token owner. */
    private UserCredentials user;

    /** token creation date. */
    private Date created = new Date();

    /** token last usage date. */
    private Date lastUsed;


    /**
     * Constructor.
     */
    public AccessToken() {

    }


    /**
     * Constructor.
     * 
     * @param token
     *            token
     * @param client
     *            client application
     * @param user
     *            token owner
     */
    public AccessToken(String token, OAuthClient client, UserCredentials user) {
        super();
        this.token = token;
        this.client = client;
        this.user = user;
    }


    /**
     * Constructor. The token value will be a random UUID.
     * 
     * @param client
     *            client application
     * @param user
     *            token owner
     */
    public AccessToken(OAuthClient client, UserCredentials user) {
        super();
        this.token = UUID.randomUUID().toString();
        this.client = client;
        this.user = user;
    }


    @Id
    @XmlElement
    public String getToken() {
        return token;
    }


    public void setToken(String token) {
        this.token = token;
    }


    @ManyToOne
    @JoinColumn(nullable = false)
    @XmlElement
    public OAuthClient getClient() {
        return client;
    }


    public void setClient(OAuthClient client) {
        this.client = client;
    }


    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    @XmlElement
    public UserCredentials getUser() {
        return user;
    }


    public void setUser(UserCredentials user) {
        this.user = user;
    }


    @Basic
    @XmlElement
    public Date getCreated() {
        return created;
    }


    public void setCreated(Date created) {
        this.created = created;
    }


    @Basic
    @XmlElement
    public Date getLastUsed() {
        return lastUsed;
    }


    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }


    /**
     * Find the access token by its value.
     * 
     * @param value
     *            access token value
     * @return access token active record
     */
    public static AccessToken findByValue(String value) {
        return findByPrimaryKey(AccessToken.class, value);
    }


    /**
     * Find an access token by its client id, user id or both.
     * 
     * @param client
     *            client
     * @param creds
     *            owner
     * @return access token active record
     */
    @SuppressWarnings("unchecked")
    public static List<AccessToken> findByClientOrUser(OAuthClient client, UserCredentials creds) {
        Criteria criteria = HibernateUtil.getSessionFactory().getCurrentSession().createCriteria(AccessToken.class);
        if (client != null) {
            criteria.add(Restrictions.eq("client.clientId", client.getClientId()));
        }
        if (creds != null) {
            criteria.add(Restrictions.eq("user.userId", creds.getUserId()));
        }
        return criteria.list();
    }

}
