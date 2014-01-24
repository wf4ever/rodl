package pl.psnc.dl.wf4ever.accesscontrol.dicts;

/**
 * Research Object access mode.
 * 
 * @author pejot
 * 
 */
public enum Mode {

    /** Visible for everyone. */
    PUBLIC,
    /** Visible for user with special permissions. */
    PRIVATE,
    /** Open for everybody for read and write. */
    OPEN;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    };
}
