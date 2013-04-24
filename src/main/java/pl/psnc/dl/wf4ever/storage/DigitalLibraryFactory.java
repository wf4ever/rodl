package pl.psnc.dl.wf4ever.storage;

import pl.psnc.dl.wf4ever.dl.DigitalLibrary;

/**
 * A factory producing digital library (storage) instances.
 * 
 * @author piotrekhol
 * 
 */
public interface DigitalLibraryFactory {

    /**
     * Return a new or existing digital library instance, in particular dLibra or filesystem DL.
     * 
     * @return a digital library
     */
    DigitalLibrary getDigitalLibrary();

}
