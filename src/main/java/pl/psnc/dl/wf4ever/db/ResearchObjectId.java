package pl.psnc.dl.wf4ever.db;

import java.io.Serializable;
import java.net.URI;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * ResearchObject ID. Once ID is taken can't be reuse any more.
 * 
 * @author pejot
 * 
 */
@Entity
@Table(name = "research_object_ids")
public class ResearchObjectId implements Serializable {

    /** Serialization. */
    private static final long serialVersionUID = 1L;
    /** The Research Object id. */
    @Id
    private String id;


    /**
     * Constructor.
     */
    public ResearchObjectId() {
        //nope
    }


    /**
     * Constructor.
     * 
     * @param id
     *            Serialized Id
     */
    public ResearchObjectId(URI id) {
        setId(id);
    }


    public URI getId() {
        return URI.create(this.id);
    }


    public void setId(URI id) {
        this.id = id.toString();
    }
}
