package pl.psnc.dl.wf4ever.evo;

import pl.psnc.dl.wf4ever.common.db.EvoType;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.RodlException;
import pl.psnc.dl.wf4ever.hibernate.HibernateUtil;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
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
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            if (status.getTarget() == null) {
                throw new OperationFailedException("Target research object must be set");
            }
            if (status.getType() == null || status.getType() == EvoType.LIVE) {
                throw new OperationFailedException("New type must be a snaphot or archive");
            }
            ResearchObject liveRO = ResearchObject.get(builder, status.getCopyfrom());
            if (liveRO == null) {
                throw new NotFoundException("Research Object not found " + status.getCopyfrom().toString());
            }
            ImmutableResearchObject immutableResearchObject = ImmutableResearchObject.get(builder, status.getTarget());
            if (immutableResearchObject == null) {
                throw new NotFoundException("Research Object not found " + status.getTarget());
            }
            //            Set<Annotation> evoAnnotations = new HashSet<>(immutableResearchObject.getAnnotationsByBodyUri().get(
            //                immutableResearchObject.getEvoInfo().getUri()));
            //            for (Annotation a : evoAnnotations) {
            //                a.delete();
            //            }
            //            immutableResearchObject.getEvoInfo().delete();
            //            immutableResearchObject.generateEvoInfo();
            immutableResearchObject.setLiveRO(liveRO);
            immutableResearchObject.getEvoInfo().save();
        } catch (RodlException e) {
            throw new OperationFailedException("Could not generate evo info", e);
        } finally {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }
    }
}
