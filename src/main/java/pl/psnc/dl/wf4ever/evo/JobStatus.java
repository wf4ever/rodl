package pl.psnc.dl.wf4ever.evo;

import java.net.URI;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import pl.psnc.dl.wf4ever.evo.Job.State;

/**
 * Job status as JSON.
 * 
 * @author piotrekhol
 * 
 */
@XmlRootElement
public class JobStatus {

    private URI copyfrom;

    private EvoType type;

    private boolean finalize;

    private URI target;

    /** job state. */
    @XmlElement(name = "status")
    private State state;

    private String reason;

    @XmlElement(name = "authorization_code")
    private String authorizationCode;


    /**
     * Default empty constructor.
     */
    public JobStatus() {

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


    public synchronized String getAuthorizationCode() {
        return authorizationCode;
    }


    public synchronized void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }


    public synchronized String getAuthorizationCodeAndSetNull() {
        String code = authorizationCode;
        authorizationCode = null;
        return code;
    }


    public synchronized void setStateAndReason(State state, String message) {
        this.state = state;
        this.reason = message;
    }

}
