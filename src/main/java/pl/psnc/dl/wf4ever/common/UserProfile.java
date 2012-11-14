/**
 * 
 */
package pl.psnc.dl.wf4ever.common;

import java.io.Serializable;
import java.net.URI;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import pl.psnc.dl.wf4ever.dl.UserMetadata;

/**
 * RODL user model.
 * 
 * @author piotrhol
 * 
 */
@Entity
@Table(name = "user_profiles")
public final class UserProfile extends UserMetadata implements Serializable {

    /** id. */
    private static final long serialVersionUID = -4468344863067565271L;


    /**
     * Constructor.
     */
    public UserProfile() {
        super();
    }


    /**
     * Constructor.
     * 
     * @param login
     *            login
     * @param name
     *            name
     * @param role
     *            role
     * @param uri
     *            uri
     */
    public UserProfile(String login, String name, Role role, URI uri) {
        super(login, name, role, uri);
    }


    /**
     * Constructor.
     * 
     * @param login
     *            login
     * @param name
     *            name
     * @param role
     *            role
     */
    public UserProfile(String login, String name, Role role) {
        super(login, name, role, null);
    }


    @Basic
    public URI getHomePage() {
        return super.getHomePage();
    }


    @Id
    public String getLogin() {
        return super.getLogin();
    }


    @Basic
    public String getName() {
        return super.getName();
    }


    @Basic
    public Role getRole() {
        return super.getRole();
    }


    @Transient
    public URI getUri() {
        return super.getUri();
    }


    /**
     * Set URI.
     * 
     * @param uri
     *            uri as string
     */
    public void setUriString(String uri) {
        super.setUri(URI.create(uri));
    }


    @Basic
    public String getUriString() {
        return super.getUri() != null ? super.getUri().toString() : null;
    }

}
