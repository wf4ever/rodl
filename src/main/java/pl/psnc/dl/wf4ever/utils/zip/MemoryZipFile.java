package pl.psnc.dl.wf4ever.utils.zip;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

/**
 * Create/Read zip file in memory.
 * 
 * @author pejot
 * 
 */
public class MemoryZipFile {

    /** Output of loaded zip file. */
    private ZipOutputStream zipOutputStream;
    /** Byte array necessary to create an output stream. */
    private ByteArrayOutputStream byteArrayOutputStream;
    /** Read/Created zip file. */
    private ZipFile zipFile;


    /**
     * Constructor.
     */
    public MemoryZipFile() {
        byteArrayOutputStream = new ByteArrayOutputStream();
        zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
    }


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
     * Create a new file as a zipEntry.
     * 
     * @param fileName
     *            entry name
     * @param data
     *            file content
     * @throws IOException .
     */
    public void addEntry(String fileName, byte[] data)
            throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(fileName));
        zipOutputStream.write(data);
        zipOutputStream.closeEntry();
    }


    /**
     * Create a new directory as a zipEntry.
     * 
     * @param dirName
     *            directory name
     * @throws IOException .
     */
    public void addEntry(String dirName)
            throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(dirName));
    }


    public byte[] getZipAsBytesArray() {
        return byteArrayOutputStream.toByteArray();
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
