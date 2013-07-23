package pl.psnc.dl.wf4ever.evo;

import java.net.URI;

import javax.xml.bind.annotation.XmlRootElement;

import pl.psnc.dl.wf4ever.job.JobStatus;

/**
 * Job status as JSON.
 * 
 * @author piotrekhol
 * 
 */
@XmlRootElement
public class CopyJobStatus extends 
JobStatus {

    /** RO to copy from. */
    private URI copyfrom;

    /** Target RO evolution status. */
    private EvoType type;

    /** Finalize? */
    private boolean finalize;

    /**
     * Default empty constructor.
     */
    public CopyJobStatus() {

    }


    /**
     * Constructor.
     * 
     * @param copyfrom
     *            RO to copy from
     * @param type
     *            Target RO evolution status
     * @param finalize
     *            Finalize?
     */
    public CopyJobStatus(URI copyfrom, EvoType type, boolean finalize) {
        setCopyfrom(copyfrom);
        setType(type);
        setFinalize(finalize);
    }


    public synchronized URI getCopyfrom() {
        return copyfrom;
    }


    public synchronized void setCopyfrom(URI copyfrom) {
        this.copyfrom = copyfrom;
    }


    public synchronized EvoType getType() {
        return type;
    }


    public synchronized void setType(EvoType type) {
        this.type = type;
    }


    public synchronized boolean isFinalize() {
        return finalize;
    }


    public void setFinalize(boolean finalize) {
        this.finalize = finalize;
    }

}
