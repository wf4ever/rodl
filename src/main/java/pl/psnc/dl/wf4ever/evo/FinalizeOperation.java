package pl.psnc.dl.wf4ever.evo;

import java.net.URI;

import pl.psnc.dl.wf4ever.common.EvoType;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.hibernate.HibernateUtil;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.model.ROEVO.ArchiveResearchObject;
import pl.psnc.dl.wf4ever.model.ROEVO.SnapshotResearchObject;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;

/**
 * Finalize research object status transformation.
 * 
 * @author piotrekhol
 * 
 */
public class FinalizeOperation implements Operation {

    /** user calling this operation. */
    private UserMetadata user;


    /**
     * Constructor.
     * 
     * @param user
     *            user calling this operation
     */
    public FinalizeOperation(UserMetadata user) {
        this.user = user;
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
            URI target = status.getCopyfrom().resolve("../" + status.getTarget() + "/");
            ResearchObject researchObject = null;
            ResearchObject liveRO = ResearchObject.get(user, status.getCopyfrom());
            if (liveRO == null) {
                throw new NotFoundException("Research Object not found " + status.getCopyfrom().toString());
            }
            switch (status.getType()) {
                case SNAPSHOT:
                    researchObject = SnapshotResearchObject.get(user, target, liveRO);
                    break;
                case ARCHIVE:
                    researchObject = ArchiveResearchObject.get(user, target, liveRO);
                    break;
                default:
                    throw new OperationFailedException("New type must be a snaphot or archive");
            }
            if (researchObject == null) {
                throw new NotFoundException("Research Object not found " + status.getTarget());
            }
            Annotation annotation = ROSRService.SMS.get().findAnnotationForBody(researchObject,
                researchObject.getFixedEvolutionAnnotationBodyUri());
            ROSRService.deleteAnnotation(researchObject, annotation.getUri());
            ROSRService
                    .deaggregateInternalResource(researchObject, researchObject.getFixedEvolutionAnnotationBodyUri());
            researchObject.generateEvoInfo();
        } catch (DigitalLibraryException | NotFoundException | AccessDeniedException e) {
            throw new OperationFailedException("Could not generate evo info", e);
        } finally {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }
    }
}
