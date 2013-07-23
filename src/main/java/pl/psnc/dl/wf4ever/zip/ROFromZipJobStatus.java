package pl.psnc.dl.wf4ever.zip;

import javax.xml.bind.annotation.XmlRootElement;

import pl.psnc.dl.wf4ever.job.JobStatus;

/**
 * The status for the job of creation RO from zip.
 * 
 * @author pejot
 * 
 */
@XmlRootElement
class ROFromZipJobStatus extends JobStatus {

    /** Constructor. */
    public ROFromZipJobStatus() {
        super();
    }
}
