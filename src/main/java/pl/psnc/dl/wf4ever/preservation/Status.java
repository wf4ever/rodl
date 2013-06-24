package pl.psnc.dl.wf4ever.preservation;

/**
 * Possible status of preserved ResearchObject.
 * 
 * @author pejot
 * 
 */
public enum Status {
    /** Research Object is new - not preserved yet. */
    NEW,
    /** Research Object was updated - preserved version is not up to date. */
    UPDATED,
    /** Research Object was deleted - needs to be deleted in context of preservation system too. */
    DELETED,
    /** Research Object wasn't deleted - the preserverd version is up to date. */
    UP_TO_DATE,
}
