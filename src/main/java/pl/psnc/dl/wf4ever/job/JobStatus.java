package pl.psnc.dl.wf4ever.job;

import java.net.URI;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import pl.psnc.dl.wf4ever.job.Job.State;

/**
 * General Job status class.
 * 
 * @author pejot
 * 
 */
@XmlRootElement
public class JobStatus {

    /** job state. */
    protected State state;
    /** Justification of the current state, useful in case of error. */
    protected String reason;
    /** Target RO URI. */
    private URI target;


    /** Constructor. */
    public JobStatus() {
        super();
    }


    @XmlElement(name = "status")
    public synchronized State getState() {
        return state;
    }


    public synchronized void setState(State state) {
        this.state = state;
    }


    public synchronized String getReason() {
        return reason;
    }


    public synchronized void setReason(String reason) {
        this.reason = reason;
    }


    public synchronized URI getTarget() {
        return target;
    }


    public synchronized void setTarget(URI target) {
        this.target = target;
    }


    /**
     * A synchronized method for setting the job status state.
     * 
     * @param state
     *            state
     * @param message
     *            explanation
     */
    public synchronized void setStateAndReason(State state, String message) {
        this.state = state;
        this.reason = message;
    }

}
