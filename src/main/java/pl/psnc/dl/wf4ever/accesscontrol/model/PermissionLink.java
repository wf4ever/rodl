package pl.psnc.dl.wf4ever.accesscontrol.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.GenericGenerator;

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
    String uuid;


    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "uuid", unique = true)
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
