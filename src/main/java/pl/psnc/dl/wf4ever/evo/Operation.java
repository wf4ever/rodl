package pl.psnc.dl.wf4ever.evo;

/**
 * An evolution operation that may be performed by the service. Currently, it can be to copy or finalize.
 * 
 * @author piotrekhol
 * 
 */
public interface Operation {

    /**
     * Execute the operation.
     * 
     * @param status
     *            job status, may be modified by the operation
     * @throws OperationFailedException
     *             the operation failed gracefully
     */
    void execute(JobStatus status)
            throws OperationFailedException;

}
