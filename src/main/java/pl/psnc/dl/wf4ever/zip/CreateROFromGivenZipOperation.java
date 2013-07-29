package pl.psnc.dl.wf4ever.zip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.job.JobStatus;
import pl.psnc.dl.wf4ever.job.Operation;
import pl.psnc.dl.wf4ever.job.OperationFailedException;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.RO.Folder;
import pl.psnc.dl.wf4ever.model.RO.FolderEntry;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.model.RO.Resource;

/**
 * Operation which stores a research object given in a zip format from outside.
 * 
 * @author pejot
 * 
 */
public class CreateROFromGivenZipOperation implements Operation {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(CreateROFromGivenZipOperation.class);
    /** resource builder. */
    private Builder builder;
    /** zip input stream. */
    InputStream zipStream;
    /** request uri info. */
    UriInfo uriInfo;
    /** Mimetypes map. */
    MimetypesFileTypeMap mfm;


    /**
     * Constructor.
     * 
     * @param builder
     *            model instance builder
     * @param zipStream
     *            zip input stream
     * @param uriInfo
     *            reqest uri info
     */
    public CreateROFromGivenZipOperation(Builder builder, InputStream zipStream, UriInfo uriInfo) {
        this.builder = builder;
        this.zipStream = zipStream;
        this.uriInfo = uriInfo;
        this.mfm = new MimetypesFileTypeMap();
    }


    @Override
    public void execute(JobStatus status)
            throws OperationFailedException {
        File tmpFile;
        try {
            tmpFile = File.createTempFile("tmp_ro", UUID.randomUUID().toString());
            BufferedInputStream inputStream = new BufferedInputStream(zipStream);
            FileOutputStream fileOutputStream;
            fileOutputStream = new FileOutputStream(tmpFile);
            IOUtils.copy(inputStream, fileOutputStream);
            fileOutputStream.close();
            inputStream.close();
        } catch (IOException e) {
            throw new OperationFailedException("Can copy input streams", e);
        }
        URI roUri = uriInfo.getBaseUri().resolve("ROs/").resolve(status.getTarget().toString() + "/");
        ResearchObject created = ResearchObject.create(builder, roUri);
        try {
            Map<String, Folder> createdFolders = new HashMap<>();
            @SuppressWarnings("resource")
            ZipFile zip = new ZipFile(tmpFile);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                addEntry(created, entry.getName(), zip.getInputStream(entry), createdFolders);
            }
        } catch (IOException | BadRequestException e) {
            throw new OperationFailedException("Can't preapre a ro from given zip", e);
        }

        status.setTarget(created.getUri());

        tmpFile.delete();
    }


    /**
     * Rewrite entry from zip to ro.
     * 
     * @param ro
     *            Research Object
     * @param name
     *            entryName
     * @param inputStream
     *            resource Input Stream
     * @param createdFolders
     *            already created folders
     * @throws BadRequestException .
     */
    private void addEntry(ResearchObject ro, String name, InputStream inputStream, Map<String, Folder> createdFolders)
            throws BadRequestException {
        //TODO can we make it more general?
        Path path = Paths.get(name);
        if (name.endsWith("/") || path.getFileName().toString().startsWith(".")) {
            LOGGER.debug("Skipping " + name + ".\n");
        } else {
            LOGGER.debug("Adding " + name + "... ");
            String contentType = mfm.getContentType(name);
            Resource resource = ro.aggregate(name, inputStream, contentType);
            boolean parentExisted = false;
            while (path.getParent() != null && !parentExisted) {
                if (!createdFolders.containsKey(path.getParent().toString())) {
                    String folderName = path.getParent().toString();
                    if (!folderName.endsWith("/")) {
                        folderName += "/";
                    }
                    //ro.getUri().resolve(path.getParent().toString() + "/")
                    Folder f = ro.aggregateFolder(UriBuilder.fromUri(ro.getUri()).path(folderName).build());
                    createdFolders.put(path.getParent().toString(), f);
                } else {
                    parentExisted = true;
                }
                Resource current = createdFolders.containsKey(path.toString()) ? createdFolders.get(path.toString())
                        : resource;
                FolderEntry entry = createdFolders.get(path.getParent().toString()).createFolderEntry(current);
                path = path.getParent();
            }
            LOGGER.debug("done.\n");
        }
    }
}
