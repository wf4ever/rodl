package pl.psnc.dl.wf4ever.util;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

/**
 * A utility class using a preconfigured {@link MimetypesFileTypeMap} instance to guess the MIME type of resources.
 * 
 * @author piotrekhol
 * 
 */
public final class MimeTypeUtil {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(MimeTypeUtil.class);

    /** The MFM instance. */
    private static MimetypesFileTypeMap mfm = initMimetypesFileTypeMap();


    /**
     * Private constructor.
     */
    private MimeTypeUtil() {
        //nope
    }


    /**
     * Initialize the {@link MimetypesFileTypeMap} with the custom MIME type mapping.
     * 
     * @return the {@link MimetypesFileTypeMap}
     */
    private static MimetypesFileTypeMap initMimetypesFileTypeMap() {
        try {
            try (InputStream mimeTypesIs = ResearchObject.class.getClassLoader().getResourceAsStream("mime.types")) {
                return new MimetypesFileTypeMap(mimeTypesIs);
            }
        } catch (IOException e) {
            LOGGER.error("Cannot read mime.types", e);
        }
        return new MimetypesFileTypeMap();
    }


    /**
     * Return the MIME type based on the specified file name. The MIME type entries are searched as described above
     * under MIME types file search order. If no entry is found, the type "application/octet-stream" is returned.
     * 
     * @param path
     *            the file path
     * @return the file's MIME type
     */
    public static String getContentType(String path) {
        return mfm.getContentType(path);
    }

}
