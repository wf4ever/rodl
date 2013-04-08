package pl.psnc.dl.wf4ever.db.dao;

import java.net.URI;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.db.ResearchObjectId;

/**
 * Research Object Id DAO.
 * 
 * @author pejot
 * 
 */
public class ResearchObjectIdDAO extends AbstractDAO<ResearchObjectId> {

    /** Serialization. */
    private static final long serialVersionUID = 1L;

    /** Logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(ResearchObjectIdDAO.class);


    /**
     * Find by URI.
     * 
     * @param id
     *            URI
     * @return the Research Object Id
     */
    public ResearchObjectId findByPrimaryKey(URI id) {
        return super.findByPrimaryKey(ResearchObjectId.class, id.toString());
    }


    /**
     * Save new ResearchObjectId instance. Throw an exception in case of duplication.
     * 
     * @param instance
     *            serialized instance.
     */
    public void save(ResearchObjectId instance) {
        if (findByPrimaryKey(instance.getId()) != null) {
            throw new IllegalArgumentException("Research Object Id duplicated" + instance.getId().toString());
        }
        super.save(instance);
    }


    /**
     * Save the given instance with the first available id. Note, the instance id may be changed. Check the id of the
     * returned instance.
     * 
     * @param instance
     *            given ResearchObjectID
     * @return the saved instance
     */
    public ResearchObjectId saftySave(ResearchObjectId instance) {
        ResearchObjectId firstFree = fistFree(instance);
        save(firstFree);
        return firstFree;
    }


    /**
     * Find first available id for given instance.
     * 
     * @param instance
     *            Research Object instance
     * @return saveable Research Object id.
     */
    public ResearchObjectId fistFree(ResearchObjectId instance) {
        if (findByPrimaryKey(instance.getId()) == null) {
            return new ResearchObjectId(instance.getId());
        }
        Integer counter = 1;
        URI searchUri = buildID(instance.getId(), counter);
        while (findByPrimaryKey(searchUri) != null) {
            searchUri = buildID(instance.getId(), counter);
            counter++;
        }
        return new ResearchObjectId(searchUri);

    }


    /**
     * Build next Uri basing on counter value.
     * 
     * @param id
     *            base id
     * @param counter
     *            counter
     * @return next Uri.
     */
    private URI buildID(URI id, Integer counter) {
        if (id.toString().length() >= 1
                && id.toString().substring(id.toString().length() - 1, id.toString().length()).equals("/")) {
            String base = id.toString().substring(0, id.toString().length() - 1);
            return URI.create(base + "-" + counter.toString() + "/");
        }

        return URI.create(id.toString() + "-" + counter.toString());
    }
}
