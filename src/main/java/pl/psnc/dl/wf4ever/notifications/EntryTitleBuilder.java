package pl.psnc.dl.wf4ever.notifications;

import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

/**
 * Build a title of the Atom Feed.
 * 
 * @author pejot
 * 
 */
public class EntryTitleBuilder {

    /**
     * Hidden constructor.
     */
    protected EntryTitleBuilder() {
        //nope 
    }


    /**
     * Build a title related to research object action.
     * 
     * @param researchObject
     *            research object
     * @param type
     *            action type
     * @return built title.
     */
    public static String buildTitle(ResearchObject researchObject, ActionType type) {
        switch (type) {
            case NEW_RO:
                return researchObject.getUri().toASCIIString() + " research object creation";
            default:
                return "";
        }
    }
}
