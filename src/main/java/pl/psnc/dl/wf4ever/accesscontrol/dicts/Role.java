package pl.psnc.dl.wf4ever.accesscontrol.dicts;

/** User role. */
public enum Role {

    /** Can read, edit and grant permissions. */
    OWNER,
    /** Can read and edit. */
    EDITOR,
    /** Can read. */
    READER;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    };
}
