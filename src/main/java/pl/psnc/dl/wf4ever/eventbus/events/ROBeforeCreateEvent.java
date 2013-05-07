package pl.psnc.dl.wf4ever.eventbus.events;

import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

/**
 * Event thrown once the new Research Object is created.
 * 
 * @author pejot
 * 
 */
public class ROBeforeCreateEvent {

    /** Event reason/subject. */
    private final ResearchObject researchObject;


    /**
     * Constructor.
     * 
     * @param researchObject
     *            reason/subject.
     */
    public ROBeforeCreateEvent(ResearchObject researchObject) {
        this.researchObject = researchObject;
    }


    public ResearchObject getResearchObject() {
        return researchObject;
    }
}
