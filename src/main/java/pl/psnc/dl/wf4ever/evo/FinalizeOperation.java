package pl.psnc.dl.wf4ever.evo;

import java.net.URI;

import pl.psnc.dl.wf4ever.common.HibernateUtil;
import pl.psnc.dl.wf4ever.common.ResearchObject;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;

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
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            if (status.getTarget() == null) {
                throw new OperationFailedException("Target research object must be set");
            }
            if (status.getType() == null) {
                throw new OperationFailedException("New type must be set");
            }
            URI target = status.getCopyfrom().resolve("../" + status.getTarget() + "/");

            ResearchObject researchObject = ResearchObject.create(target);
            ResearchObject liveRO = ResearchObject.create(status.getCopyfrom());

            Annotation annotation = ROSRService.SMS.get().findAnnotationForBody(researchObject,
                researchObject.getFixedEvolutionAnnotationBodyUri());
            ROSRService.deleteAnnotation(researchObject, annotation.getUri());
            ROSRService
                    .deaggregateInternalResource(researchObject, researchObject.getFixedEvolutionAnnotationBodyUri());
            ROSRService.generateEvoInfo(researchObject, liveRO, status.getType());
        } catch (DigitalLibraryException | NotFoundException | AccessDeniedException e) {
            throw new OperationFailedException("Could not generate evo info", e);
        } finally {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }
    }
}
