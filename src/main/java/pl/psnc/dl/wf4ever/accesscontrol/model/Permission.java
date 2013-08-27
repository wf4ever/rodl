package pl.psnc.dl.wf4ever.accesscontrol.model;

import java.net.URI;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import pl.psnc.dl.wf4ever.accesscontrol.dicts.Role;
import pl.psnc.dl.wf4ever.db.UserProfile;

/**
 * Data produced/received by permission API.
 * 
 * @author pejot
 * 
 */
@Entity
@Table(name = "permissions")
@XmlRootElement(name = "permission")
public class Permission {

    /** Unique id. */
    private int id;
    /** Research Object uri. */
    private String roUri;
    /** Object location. */
    private URI uri;
    /** User id (openid uri). */
    private UserProfile user;
    /** User role. */
    private Role role;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }


    @XmlElement(name = "ro")
    @Column(name = "ro", unique = true)
    public String getRo() {
        return roUri;
    }


    public void setRo(String roUri) {
        this.roUri = roUri;
    }


    @XmlElement(name = "user")
    @ManyToOne
    @JoinColumn(nullable = false)
    public UserProfile getUser() {
        return user;
    }


    public void setUser(UserProfile user) {
        this.user = user;
    }


    public Role getRole() {
        return role;
    }


    public void setRole(Role role) {
        this.role = role;
    }


    @Transient
    public URI getUri() {
        return uri;
    }


    public void setUri(URI uri) {
        this.uri = uri;
    }
}
