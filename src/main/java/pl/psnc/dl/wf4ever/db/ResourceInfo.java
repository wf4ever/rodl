package pl.psnc.dl.wf4ever.db;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.ResourceMetadata;

/**
 * File metadata.
 * 
 * @author piotrhol
 * 
 */
@Entity
@Table(name = "resource_infos")
public final class ResourceInfo extends ResourceMetadata implements Serializable {

    /** id. */
    private static final long serialVersionUID = 6130642871779327154L;


    /**
     * Constructor.
     */
    public ResourceInfo() {
        super();
    }


    /**
     * Constructor.
     * 
     * @param path
     *            file path
     * @param name
     *            file name
     * @param checksum
     *            checksum
     * @param sizeInBytes
     *            size in bytes
     * @param digestMethod
     *            i.e. MD5, SHA1
     * @param lastModified
     *            date of last modification
     * @param mimeType
     *            MIME type
     */
    public ResourceInfo(String path, String name, String checksum, long sizeInBytes, String digestMethod,
            DateTime lastModified, String mimeType) {
        super(path, name, checksum, sizeInBytes, digestMethod, lastModified, mimeType);
    }


    @Basic
    public String getName() {
        return super.getName();
    }


    @Basic
    public String getChecksum() {
        return super.getChecksum();
    }


    @Basic
    public long getSizeInBytes() {
        return super.getSizeInBytes();
    }


    @Basic
    public String getDigestMethod() {
        return super.getDigestMethod();
    }


    @Transient
    public DateTime getLastModified() {
        return super.getLastModified();
    }


    @Basic
    public long getLastModifiedInMilis() {
        return super.getLastModified() != null ? super.getLastModified().getMillis() : null;
    }


    /**
     * Set last modified date.
     * 
     * @param milis
     *            miliseconds
     */
    public void setLastModifiedInMilis(long milis) {
        super.setLastModified(new DateTime(milis));
    }


    @Basic
    public String getMimeType() {
        return super.getMimeType();
    }


    @Id
    @Column(length = 128)
    public String getPath() {
        return super.getPath();
    }

}
