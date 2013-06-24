package pl.psnc.dl.wf4ever.preservation;

import java.net.URI;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The status of preserved Research Object.
 * 
 * @author pejot
 * 
 */
@Entity
@Table(name = "research_object_preservation_statuses")
public class ResearchObjectPreservationStatus {

    /**
     * The described Research Object.
     */
    @Id
    private String researchObjectUri;

    /**
     * The status of preserved copy.
     */
    @Column(nullable = false)
    private Status status;


    /**
     * Empty constructor.
     */
    public ResearchObjectPreservationStatus() {

    }


    /**
     * Constructor.
     * 
     * @param researchObjectUri
     *            the uri of preserved Research Object
     * @param status
     *            the currect preservation status
     */
    public ResearchObjectPreservationStatus(URI researchObjectUri, Status status) {
        setResearchObjectUri(researchObjectUri);
        setStatus(status);
    }


    public String getResearchObjectUri() {
        return researchObjectUri;
    }


    public void setResearchObjectUri(URI researchObjectUri) {
        this.researchObjectUri = researchObjectUri.toString();
    }


    public void setResearchObjectUri(String researchObjectUri) {
        this.researchObjectUri = researchObjectUri;
    }


    public Status getStatus() {
        return status;
    }


    public void setStatus(Status status) {
        this.status = status;
    }
}
