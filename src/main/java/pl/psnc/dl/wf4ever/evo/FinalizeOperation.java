package pl.psnc.dl.wf4ever.evo;

import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.RodlException;
import pl.psnc.dl.wf4ever.job.JobStatus;
import pl.psnc.dl.wf4ever.job.Operation;
import pl.psnc.dl.wf4ever.job.OperationFailedException;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.ROEVO.ImmutableResearchObject;

/**
 * Finalize research object status transformation.
 * 
 * @author piotrekhol
 * 
 */
public class FinalizeOperation implements Operation {

    /** resource builder. */
    private Builder builder;


    /**
     * Constructor.
     * 
     * @param builder
     *            user calling this operation
     */
    public FinalizeOperation(Builder builder) {
        this.builder = builder;
    }


    @Override
    public void execute(JobStatus status)
            throws OperationFailedException {
        HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().begin();
        try {
            if (!(status instanceof CopyJobStatus)) {
                throw new OperationFailedException("Given JobStatus is not a instance of CopyJobStatus");
            }
            CopyJobStatus copyJobStatus = (CopyJobStatus) status;
            if (copyJobStatus.getTarget() == null) {
                throw new OperationFailedException("Target research object must be set");
            }
            if (copyJobStatus.getType() == null || copyJobStatus.getType() == EvoType.LIVE) {
                throw new OperationFailedException("New type must be a snaphot or archive");
            }
            ImmutableResearchObject immutableResearchObject = ImmutableResearchObject.get(builder,
                copyJobStatus.getTarget());
            if (immutableResearchObject == null) {
                throw new NotFoundException("Research Object not found " + copyJobStatus.getTarget());
            }
            immutableResearchObject.setFinalized(true);
            immutableResearchObject.getEvoInfo().updateHistory();
        } catch (RodlException e) {
            throw new OperationFailedException("Could not generate evo info", e);
        } finally {
            builder.getEventBusModule().commit();
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }
    }
}
