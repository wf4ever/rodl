package pl.psnc.dl.wf4ever.rosrs;

import pl.psnc.dl.wf4ever.dl.DigitalLibrary;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;

/**
 * Utility class for distributing the RO tasks to dLibra and SMS.
 * 
 * @author piotrekhol
 * 
 */
public final class ROSRService {

    /** Thread local DL instance. */
    public static final ThreadLocal<DigitalLibrary> DL = new ThreadLocal<>();

    /** Thread local SMS instance. */
    public static final ThreadLocal<SemanticMetadataService> SMS = new ThreadLocal<>();


    /**
     * Private constructor.
     */
    private ROSRService() {
        //nope
    }
}
