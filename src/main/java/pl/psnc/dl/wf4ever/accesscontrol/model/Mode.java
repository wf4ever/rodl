package pl.psnc.dl.wf4ever.accesscontrol.model;

import java.net.URI;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Mode model produced and consumed by Resource Mode API.
 * 
 * @author pejot
 * 
 */
@Entity
@Table(name = "modes")
@XmlRootElement(name = "mode")
public class Mode {

    /** Unique id. */
    private Integer id;
    /** Object location. */
    private URI uri;
    /** Research Object uri. */
    private String roUri;
    /** Research Object access mode. */
    private pl.psnc.dl.wf4ever.accesscontrol.dicts.Mode mode;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @XmlTransient
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


    public pl.psnc.dl.wf4ever.accesscontrol.dicts.Mode getMode() {
        return mode;
    }


    public void setMode(pl.psnc.dl.wf4ever.accesscontrol.dicts.Mode mode) {
        this.mode = mode;
    }


    @Transient
    public URI getUri() {
        return uri;
    }


    public void setUri(URI uri) {
        this.uri = uri;
    }

}
