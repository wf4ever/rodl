package pl.psnc.dl.wf4ever;

/**
 * RO model and HTTP request cycle constants. This class should be refactored.
 * 
 * @author piotrhol
 * 
 */
public final class Constants {

    /**
     * No instances allowed.
     */
    private Constants() {
        // nop
    }


    /** HTTP Slug header. */
    public static final String SLUG_HEADER = "Slug";

    /** See ROSR API. */
    public static final String PROXY_MIME_TYPE = "application/vnd.wf4ever.proxy";

    /** See ROSR API. */
    public static final String ANNOTATION_MIME_TYPE = "application/vnd.wf4ever.annotation";

    /** See ROSR API. */
    public static final String ACCEPT_HEADER = "Accept";

    /** See ROSR API. */
    public static final String LINK_HEADER = "Link";

    /** See ROSR API. */
    public static final String LINK_HEADER_TEMPLATE = "<%s>; rel=\"%s\"";

    /** See ROSR API. */
    public static final String FOLDER_MIME_TYPE = "application/vnd.wf4ever.folder";

    /** See ROSR API. */
    public static final String FOLDERENTRY_MIME_TYPE = "application/vnd.wf4ever.folderentry";
}
