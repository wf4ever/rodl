/**
 * 
 */
package pl.psnc.dl.wf4ever.auth;

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

import pl.psnc.dl.wf4ever.common.ActiveRecord;

/**
 * User credentials DAO.
 * 
 * @author Piotr Hołubowicz
 * 
 */
@Entity
@Table(name = "usercredentials")
@XmlRootElement
public class UserCredentials extends ActiveRecord {

    /** id. */
    private static final long serialVersionUID = 7967488547563569388L;

    /** A user credentials constant representing a request with no authentication information. */
    public static final UserCredentials PUBLIC_USER = new UserCredentials("wf4ever_reader", "wf4ever_reader!!!");

    /** user id. */
    private String userId;

    /** user dLibra password. */
    private String password;

    /** access tokens owned by the user. */
    private List<AccessToken> tokens = new ArrayList<AccessToken>();


    /**
     * Constructor.
     */
    public UserCredentials() {
    }


    /**
     * Constructor.
     * 
     * @param userId
     *            user id
     * @param password
     *            user dLibra password
     * @throws IllegalArgumentException
     *             the parameters are null
     */
    public UserCredentials(String userId, String password)
            throws IllegalArgumentException {
        if (userId == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        this.userId = userId;
        this.password = password;
    }


    @Id
    @XmlElement
    public String getUserId() {
        return userId;
    }


    public void setUserId(String userId) {
        this.userId = userId;
    }


    @Basic
    @XmlTransient
    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
    @XmlTransient
    public List<AccessToken> getTokens() {
        return tokens;
    }


    public void setTokens(List<AccessToken> tokens) {
        this.tokens = tokens;
    }


    /**
     * Find the user credentials in the database.
     * 
     * @param userId
     *            user id
     * @return user credentials from the database or null
     */
    public static UserCredentials findByUserId(String userId) {
        return findByPrimaryKey(UserCredentials.class, userId);
    }

}
