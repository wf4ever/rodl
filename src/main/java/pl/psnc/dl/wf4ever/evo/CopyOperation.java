package pl.psnc.dl.wf4ever.evo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
import pl.psnc.dl.wf4ever.dl.RodlException;
import pl.psnc.dl.wf4ever.job.JobStatus;
import pl.psnc.dl.wf4ever.job.Operation;
import pl.psnc.dl.wf4ever.job.OperationFailedException;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.model.ROEVO.ImmutableResearchObject;

/**
 * Copy one research object to another.
 * 
 * @author piotrekhol
 * 
 */
public class CopyOperation implements Operation {

    /** logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(CopyOperation.class);

    /** resource builder. */
    private Builder builder;


    /**
     * Constructor.
     * 
     * @param builder
     *            model instance builder
     */
    public CopyOperation(Builder builder) {
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
            ResearchObject sourceRO = ResearchObject.get(builder, copyJobStatus.getCopyfrom());
            if (sourceRO == null) {
                throw new OperationFailedException("source Research Object does not exist");
            }
            try {
                ImmutableResearchObject.create(copyJobStatus.getTarget(), sourceRO, builder, copyJobStatus.getType());
            } catch (RodlException e) {
                throw new OperationFailedException("Failed to copy RO", e);
            }
        } finally {
            builder.getEventBusModule().commit();
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }

    }

}
