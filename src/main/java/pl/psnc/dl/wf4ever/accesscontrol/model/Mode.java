package pl.psnc.dl.wf4ever.accesscontrol.model;

import java.net.URI;

/**
 * Mode model produced and consumed by Resource Mode API.
 * 
 * @author pejot
 * 
 */
public class Mode {

    /** Unique id. */
    private URI id;
    /** Research Object uri. */
    private URI roUri;
    /** Research Object access mode. */
    private pl.psnc.dl.wf4ever.accesscontrol.dicts.Mode mode;


    public URI getId() {
        return id;
    }


    public void setId(URI id) {
        this.id = id;
    }


    public URI getRoUri() {
        return roUri;
    }


    public void setRoUri(URI roUri) {
        this.roUri = roUri;
    }


    public pl.psnc.dl.wf4ever.accesscontrol.dicts.Mode getMode() {
        return mode;
    }


    public void setMode(pl.psnc.dl.wf4ever.accesscontrol.dicts.Mode mode) {
        this.mode = mode;
    }

}
