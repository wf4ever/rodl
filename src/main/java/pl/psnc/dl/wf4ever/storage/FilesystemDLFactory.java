package pl.psnc.dl.wf4ever.storage;

import java.util.Properties;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.dl.DigitalLibrary;
import pl.psnc.dl.wf4ever.fs.FilesystemDL;

/**
 * A factory creating a filesystem storage backend.
 * 
 * @author piotrekhol
 * 
 */
public class FilesystemDLFactory implements DigitalLibraryFactory {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(FilesystemDLFactory.class);

    /** the root folder for filesystem storage. */
    private static String filesystemBase;


    /**
     * Constructor.
     * 
     * @param properties
     *            a properties file to load any necessary properties
     */
    public FilesystemDLFactory(Properties properties) {
        filesystemBase = properties.getProperty("filesystemBase", "/tmp/dl/");
        LOGGER.debug("Filesystem base: " + filesystemBase);
    }


    @Override
    public DigitalLibrary getDigitalLibrary() {
        return new FilesystemDL(filesystemBase);
    }

}
