package pl.psnc.dl.wf4ever.evo;

import java.io.Serializable;
import java.net.URI;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RoCopy implements Serializable {

    /** id. */
    private static final long serialVersionUID = -281761366120369420L;


    public enum Type {
        LIVE,
        SNAPSHOT,
        ARCHIVED
    }


    private URI copyFrom;

    private Type type;

    private boolean finalize = false;


    public RoCopy(URI copyFrom, Type type, boolean finalize) {
        super();
        this.copyFrom = copyFrom;
        this.type = type;
        this.finalize = finalize;
    }


    public URI getCopyFrom() {
        return copyFrom;
    }


    public void setCopyFrom(URI copyFrom) {
        this.copyFrom = copyFrom;
    }


    public Type getType() {
        return type;
    }


    public void setType(Type type) {
        this.type = type;
    }


    public boolean isFinalize() {
        return finalize;
    }


    public void setFinalize(boolean finalize) {
        this.finalize = finalize;
    }

}
