package pl.psnc.dl.wf4ever.utils.zip;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.UnmappableCharacterException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

public class MemoryZipFile {

    ZipOutputStream zipOutputStream;
    ByteArrayOutputStream byteArrayOutputStream;
    ZipFile zipFile;
    public ZipFile get() {
        return zipFile;
    }

    public MemoryZipFile() {
        byteArrayOutputStream = new ByteArrayOutputStream();
        zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
    }


    public MemoryZipFile(File file)
            throws ZipException, IOException {
        zipFile = new ZipFile(file);
    }


    public void AddEntry(String fileName, byte[] data)
            throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(fileName));
        zipOutputStream.write(data);
        zipOutputStream.closeEntry();
    }


    public void AddEntry(String dirName)
            throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(dirName));
    }


    public byte[] getZipAsBytesArray() {
        return byteArrayOutputStream.toByteArray();
    }


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