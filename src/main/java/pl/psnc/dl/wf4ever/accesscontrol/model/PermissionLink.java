package pl.psnc.dl.wf4ever.accesscontrol.model;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Data produced/received by permission API.
 * 
 * @author pejot
 * 
 */
@Entity
@Table(name = "permission_links")
@XmlRootElement(name = "permission_link")
public class PermissionLink extends Permission {

    /** Unique id. */
    private String uuid;


    public String getUuid() {
        return uuid;
    }


    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public void setUuid(UUID uuid) {
        this.uuid = uuid.toString();
    }

}
