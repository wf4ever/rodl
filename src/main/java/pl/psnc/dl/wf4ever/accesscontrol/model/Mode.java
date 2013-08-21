package pl.psnc.dl.wf4ever.accesscontrol.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Mode model produced and consumed by Resource Mode API.
 * 
 * @author pejot
 * 
 */
@XmlRootElement
@Entity
public class Mode {

    /** Unique id. */
    private String id;
    /** Research Object uri. */
    private String roUri;
    /** Research Object access mode. */
    private pl.psnc.dl.wf4ever.accesscontrol.dicts.Mode mode;


    @Id
    @XmlElement(name = "uri")
    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getRoUri() {
        return roUri;
    }


    @XmlElement(name = "ro")
    public void setRoUri(String roUri) {
        this.roUri = roUri;
    }


    public pl.psnc.dl.wf4ever.accesscontrol.dicts.Mode getMode() {
        return mode;
    }


    public void setMode(pl.psnc.dl.wf4ever.accesscontrol.dicts.Mode mode) {
        this.mode = mode;
    }

}
