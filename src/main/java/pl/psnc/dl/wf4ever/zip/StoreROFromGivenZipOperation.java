package pl.psnc.dl.wf4ever.zip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;

import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.job.JobStatus;
import pl.psnc.dl.wf4ever.job.Operation;
import pl.psnc.dl.wf4ever.job.OperationFailedException;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.util.MemoryZipFile;

/**
 * Operation which stores a research object given in a zip format from outside.
 * 
 * @author pejot
 * 
 */
public class StoreROFromGivenZipOperation implements Operation {

    /** resource builder. */
    private Builder builder;
    /** zip input stream. */
    InputStream zipStream;
    /** request uri info. */
    UriInfo uriInfo;


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
    public StoreROFromGivenZipOperation(Builder builder, InputStream zipStream, UriInfo uriInfo) {
        this.builder = builder;
        this.zipStream = zipStream;
        this.uriInfo = uriInfo;
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
        } catch (IOException e) {
            throw new OperationFailedException("Can copy input streams", e);
        }
        URI roUri = uriInfo.getBaseUri().resolve("ROs/").resolve(status.getTarget().toString() + "/");
        try {
            ResearchObject created = ResearchObject.create(builder, roUri, new MemoryZipFile(tmpFile, status
                    .getTarget().toString()));
            status.setTarget(created.getUri());
        } catch (IOException | BadRequestException e) {
            throw new OperationFailedException("Can't preapre a ro from given zip", e);
        }
        tmpFile.delete();
    }
}
