package pl.psnc.dl.wf4ever.evo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.dl.RodlException;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.hibernate.HibernateUtil;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.RO.Folder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

/**
 * Copy one research object to another.
 * 
 * @author piotrekhol
 * 
 */
public class CopyOperation implements Operation {

    /** logger. */
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
            ResearchObject targetRO;
            try {
                targetRO = ResearchObject.create(builder, status.getTarget());
            } catch (RodlException e) {
                throw new OperationFailedException("Failed to create target RO", e);
            }
            ResearchObject sourceRO = ResearchObject.get(builder, status.getCopyfrom());
            if (sourceRO == null) {
                throw new OperationFailedException("source Research Object does not exist");
            }
            // copy the ro:Resources
            for (pl.psnc.dl.wf4ever.model.RO.Resource resource : sourceRO.getResources().values()) {
                try {
                    targetRO.copy(resource);
                } catch (BadRequestException e) {
                    LOGGER.warn("Failed to copy the resource", e);
                }
            }
            //copy the annotations
            for (Annotation annotation : sourceRO.getAnnotations().values()) {
                try {
                    targetRO.copy(annotation);
                } catch (BadRequestException e) {
                    LOGGER.warn("Failed to copy the annotation", e);
                }
            }
            //copy the folders
            for (Folder folder : sourceRO.getFolders().values()) {
                targetRO.copy(folder);
            }
            //TODO!!
            //make me easier!
            //            Annotation a = targetRO.getAnnotationsByBodyUri().get(targetRO.getEvoInfoBody().getUri()).iterator().next();
            //            a.getBody().delete();
        } finally {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        }

    }

}
