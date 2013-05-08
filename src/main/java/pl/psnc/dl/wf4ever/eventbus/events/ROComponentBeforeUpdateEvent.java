package pl.psnc.dl.wf4ever.eventbus.events;

import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectComponentSerializable;

/**
 * Event thrown once the new Research Object is created.
 * 
 * @author pejot
 * 
 */
public class ROComponentBeforeUpdateEvent {

    /** Event reason/subject. */
    private final ResearchObjectComponentSerializable researchObjectComponent;


    /**
     * Constructor.
     * 
     * @param researchObjectComponent
     *            reason/subject.
     */
    public ROComponentBeforeUpdateEvent(ResearchObjectComponentSerializable researchObjectComponent) {
        this.researchObjectComponent = researchObjectComponent;
    }


    public ResearchObjectComponentSerializable getResearchObject() {
        return researchObjectComponent;
    }
}
