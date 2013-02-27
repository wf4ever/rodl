package pl.psnc.dl.wf4ever.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

/**
 * Create/Read zip file in memory.
 * 
 * @author pejot
 * 
 */
public class MemoryZipFile {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(MemoryZipFile.class);

    /** Read/Created zip file. */
    private final ZipFile zipFile;

    /** Optional prefix if the content is stored inside a folder. */
    private String prefix;


    /**
     * Constructor.
     * 
     * @param file
     *            the zip file
     * @param roName
     *            RO name, useful if the RO is inside a folder
     * @throws ZipException
     *             if a ZIP format error has occurred when creating a {@link ZipFile}
     * @throws IOException
     *             if a ZIP format error has occurred when creating a {@link ZipFile}
     */
    public MemoryZipFile(File file, String roName)
            throws ZipException, IOException {
        zipFile = new ZipFile(file);
        prefix = "";
        if (getManifestAsInputStream() == null) {
            prefix = roName.endsWith("/") ? roName : roName.concat("/");
            if (getManifestAsInputStream() == null) {
                throw new IllegalArgumentException("Cannot find a manifest entry in the zip file");
            }
        }
    }


    public ZipFile getZipFile() {
        return zipFile;
    }


    /**
     * Get a certain entry as an InputStream.
     * 
     * @param entryName
     *            file name
     * @return file content as an input stream
     */
    public InputStream getEntryAsStream(String entryName) {
        ZipEntry entry = zipFile.getEntry(prefix + entryName);
        if (entry == null) {
            return null;
        }
        try {
            return zipFile.getInputStream(entry);
        } catch (IOException | NullPointerException e) {
            LOGGER.warn("Error when looking for ZIP entry", e);
            return null;
        }
    }


    /**
     * Check if the zip contains certain entry.
     * 
     * @param entryName
     *            .
     * @return true if contain, false otherwise.
     */
    public boolean containsEntry(String entryName) {
        return zipFile.getEntry(prefix + entryName) != null;
    }


    public InputStream getManifestAsInputStream() {
        return getEntryAsStream(ResearchObject.MANIFEST_PATH);
    }
}
