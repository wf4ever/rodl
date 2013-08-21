package pl.psnc.dl.wf4ever.accesscontrol.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@XmlRootElement
public class Permission {

    /** Unique id. */
    private String id;
    /** Research Object uri. */
    private String roUri;
    /** User id (openid uri). */
    private UserProfile user;
    /** User role. */
    private Role role;


    @Id
    @XmlElement(name = "uri")
    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    @XmlElement(name = "ro")
    public String getRoUri() {
        return roUri;
    }


    public void setRoUri(String roUri) {
        this.roUri = roUri;
    }


    @XmlElement(name = "user")
    @ManyToOne
    @JoinColumn(nullable = false)
    public UserProfile getUserId() {
        return user;
    }


    public void setUserId(UserProfile user) {
        this.user = user;
    }


    public Role getRole() {
        return role;
    }


    public void setRole(Role role) {
        this.role = role;
    }
}
