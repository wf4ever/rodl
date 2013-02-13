package pl.psnc.dl.wf4ever.evo;

import java.net.URI;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import pl.psnc.dl.wf4ever.common.db.EvoType;
import pl.psnc.dl.wf4ever.evo.Job.State;

/**
 * Job status as JSON.
 * 
 * @author piotrekhol
 * 
 */
@XmlRootElement
public class JobStatus {

    /** RO to copy from. */
    private URI copyfrom;

    /** Target RO evolution status. */
    private EvoType type;

    /** Finalize? */
    private boolean finalize;

    /** Target RO URI. */
    private URI target;

    /** job state. */
    private State state;

    /** Justification of the current state, useful in case of error. */
    private String reason;


    /**
     * Default empty constructor.
     */
    public JobStatus() {

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
    public JobStatus(URI copyfrom, EvoType type, boolean finalize) {
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


    public synchronized URI getTarget() {
        return target;
    }


    public synchronized void setTarget(URI target) {
        this.target = target;
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
