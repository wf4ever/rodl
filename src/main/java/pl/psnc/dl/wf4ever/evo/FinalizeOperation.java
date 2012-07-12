package pl.psnc.dl.wf4ever.evo;

import pl.psnc.dl.wf4ever.auth.SecurityFilter;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;

/**
 * Finalize research object status transformation.
 * 
 * @author piotrekhol
 * 
 */
public class FinalizeOperation implements Operation {

    @Override
    public void execute(JobStatus status)
            throws OperationFailedException {
        if (status.getTarget() == null) {
            throw new OperationFailedException("Target research object must be set");
        }
        if (status.getType() == null) {
            throw new OperationFailedException("New type must be set");
        }
        switch (status.getType()) {
            default:
            case LIVE:
                SecurityFilter.SMS.get().setEvolutionClass(status.getTarget(),
                    SemanticMetadataService.EvolutionClass.LIVE);
                break;
            case SNAPSHOT:
                SecurityFilter.SMS.get().setEvolutionClass(status.getTarget(),
                    SemanticMetadataService.EvolutionClass.SNAPSHOT);
                break;
            case ARCHIVED:
                SecurityFilter.SMS.get().setEvolutionClass(status.getTarget(),
                    SemanticMetadataService.EvolutionClass.ARCHIVED);
                break;
        }
    }
}
