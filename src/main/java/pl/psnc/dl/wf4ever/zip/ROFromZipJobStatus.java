package pl.psnc.dl.wf4ever.zip;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import pl.psnc.dl.wf4ever.job.JobStatus;

/**
 * The status for the job of creation RO from zip.
 * 
 * @author pejot
 * 
 */
@XmlRootElement
public class ROFromZipJobStatus extends JobStatus {

    /** Number of resources which were submitted. */
    protected Integer submittedResources;

    /** Number of resources which were already processed. */
    protected Integer processedResources;


    /** Constructor. */
    public ROFromZipJobStatus() {
        super();
    }


    @XmlElement(name = "submitted_resources")
    public Integer getSubmittedResources() {
        return submittedResources;
    }


    public void setSubmittedResources(Integer submittedResources) {
        this.submittedResources = submittedResources;
    }


    @XmlElement(name = "processed_resources")
    public Integer getProcessedResources() {
        return processedResources;
    }


    public void setProcessedResources(Integer processedResource) {
        this.processedResources = processedResource;
    }
}
