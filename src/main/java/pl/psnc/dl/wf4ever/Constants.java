package pl.psnc.dl.wf4ever;

/**
 * 
 * @author nowakm
 * 
 */
public final class Constants {

    /**
     * No instances allowed
     */
    private Constants() {
        // nop
    }


    /**
     * Used for accessing DLibraDataSource stored in HttpRequest
     */
    public static final String USER = "user";

    public static final String workspaceId = "default";

    public static final String versionId = "v1";

    public static final String SLUG_HEADER = "Slug";

    public static final String PROXY_MIME_TYPE = "application/vnd.wf4ever.proxy";

    public static final String ANNOTATION_MIME_TYPE = "application/vnd.wf4ever.annotation";
}
