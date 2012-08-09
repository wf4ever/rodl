package pl.psnc.dl.wf4ever.evo;


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
    }
}
