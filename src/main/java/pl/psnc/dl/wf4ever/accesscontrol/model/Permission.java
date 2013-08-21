package pl.psnc.dl.wf4ever.accesscontrol.model;

import java.net.URI;

import pl.psnc.dl.wf4ever.accesscontrol.dicts.Role;

/**
 * Data produced/received by permission API.
 * 
 * @author pejot
 * 
 */
public class Permission {

    /** Unique id. */
    private URI id;
    /** Research Object uri. */
    private URI roUri;
    /** User id (open id uri). */
    private URI userId;
    /** User role. */
    private Role role;


    public URI getId() {
        return id;
    }


    public void setId(URI id) {
        this.id = id;
    }


    public URI getRoUri() {
        return roUri;
    }


    public void setRoUri(URI roUri) {
        this.roUri = roUri;
    }


    public URI getUserId() {
        return userId;
    }


    public void setUserId(URI userId) {
        this.userId = userId;
    }


    public Role getRole() {
        return role;
    }


    public void setRole(Role role) {
        this.role = role;
    }
}
