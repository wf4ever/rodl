/**
 * 
 */
package pl.psnc.dl.wf4ever.job;


/**
 * Interface for a job container, which means a class that should be notified about job changes.
 * 
 * @author piotrekhol
 * 
 */
public interface JobsContainer {

    /**
     * Called when a job expires and should be deleted from memory.
     * 
     * @param job
     *            the job
     */
    void onJobDone(Job job);

}
