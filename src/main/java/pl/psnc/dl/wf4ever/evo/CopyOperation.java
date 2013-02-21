package pl.psnc.dl.wf4ever.evo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.dl.RodlException;
import pl.psnc.dl.wf4ever.hibernate.HibernateUtil;
import pl.psnc.dl.wf4ever.model.ArchiveBuilder;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.EvoBuilder;
import pl.psnc.dl.wf4ever.model.SnapshotBuilder;
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
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            ResearchObject sourceRO = ResearchObject.get(builder, status.getCopyfrom());
            if (sourceRO == null) {
                throw new OperationFailedException("source Research Object does not exist");
            }
            EvoBuilder evoBuilder;
            //TODO can we make it a static EvoBuilder method?
            switch (status.getType()) {
                case SNAPSHOT:
                    evoBuilder = new SnapshotBuilder();
                    break;
                case ARCHIVE:
                    evoBuilder = new ArchiveBuilder();
                    break;
                default:
                    throw new OperationFailedException("Unsupported evolution type: " + status.getType());
            }
            try {
                ImmutableResearchObject.create(status.getTarget(), sourceRO, builder, evoBuilder);
            } catch (RodlException e) {
                throw new OperationFailedException("Failed to copy RO", e);
            }
        } finally {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }

    }

}
