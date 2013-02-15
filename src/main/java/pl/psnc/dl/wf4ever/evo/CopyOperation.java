package pl.psnc.dl.wf4ever.evo;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.dl.RodlException;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.hibernate.HibernateUtil;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.RO.Folder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;

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
            Map<URI, URI> changedURIs = new HashMap<>();
            changedURIs.put(sourceRO.getManifestUri(), targetRO.getManifestUri());
            // copy the ro:Resources
            for (pl.psnc.dl.wf4ever.model.RO.Resource resource : sourceRO.getResources().values()) {
                try {
                    pl.psnc.dl.wf4ever.model.RO.Resource resource2 = targetRO.copy(resource);
                    changedURIs.put(resource.getUri(), resource2.getUri());
                } catch (BadRequestException e) {
                    LOGGER.warn("Failed to copy the resource", e);
                }
            }
            //copy the annotations
            for (Annotation annotation : sourceRO.getAnnotations().values()) {
                try {
                    Annotation annotation2 = targetRO.copy(annotation);
                    changedURIs.put(annotation.getUri(), annotation2.getUri());
                } catch (BadRequestException e) {
                    LOGGER.warn("Failed to copy the annotation", e);
                }
            }
            //copy the folders
            for (Folder folder : sourceRO.getFolders().values()) {
                Folder folder2 = targetRO.copy(folder);
                changedURIs.put(folder.getUri(), folder2.getUri());
            }
            //            for (Map.Entry<URI, URI> e : changedURIs.entrySet()) {
            //                ROSRService.SMS.get().changeURIInManifestAndAnnotationBodies(targetRO, e.getKey(), e.getValue(), false);
            //            }
            for (Map.Entry<URI, URI> e : changedURIs.entrySet()) {
                int c = ROSRService.SMS.get().changeURIInManifestAndAnnotationBodies(targetRO, e.getKey(),
                    e.getValue(), true);
                if (c > 0) {
                    System.out.println(c);
                }
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
