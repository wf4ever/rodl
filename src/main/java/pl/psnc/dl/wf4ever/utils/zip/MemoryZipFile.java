package pl.psnc.dl.wf4ever.utils.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

/**
 * Create/Read zip file in memory.
 * 
 * @author pejot
 * 
 */
public class MemoryZipFile {

    private ZipFile zipFile;


    /**
     * Constructor.
     * 
     * @param file
     *            the zip file
     * @throws ZipException .
     * @throws IOException .
     */
    public MemoryZipFile(File file)
            throws ZipException, IOException {
        zipFile = new ZipFile(file);
    }


    public ZipFile getZipFile() {
        return zipFile;
    }


    /**
     * Get a certain entry from the zip file.
     * 
     * @param entryName
     *            file name
     * @return file as a byteArray
     */
    public byte[] getEntry(String entryName) {

        try {
            return IOUtils.toByteArray(zipFile.getInputStream(zipFile.getEntry(entryName)));
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }


    public String getManifest() {
        return new String(getEntry(".ro/manifest.rdf"));
    }


    /**
     * Get a certain entry as an Input Stream.
     * 
     * @param entryName
     *            file name
     * @return file content as an input stream
     */
    public InputStream getEntryAsStream(String entryName) {
        try {
            return zipFile.getInputStream(zipFile.getEntry(entryName));
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }


    public InputStream getManifestAsInputStream() {
        return getEntryAsStream(".ro/manifest.rdf");
    }
}
