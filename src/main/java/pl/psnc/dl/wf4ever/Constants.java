package pl.psnc.dl.wf4ever;

import java.util.regex.Pattern;

/**
 * 
 * @author piotrhol
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

    public static final String AO_ANNOTATES_HEADER = "http://purl.org/ao/annotates";

    public static final String ORE_PROXY_FOR_HEADER = "http://www.openarchives.org/ore/terms/proxyFor";

    public static final String PROXY_MIME_TYPE = "application/vnd.wf4ever.proxy";

    public static final String ANNOTATION_MIME_TYPE = "application/vnd.wf4ever.annotation";

    public static final String ACCEPT_HEADER = "Accept";

    public static final String MANIFEST_PATH = ".ro/manifest.rdf";

    public static final String LINK_HEADER = "Link";

    public static final String LINK_HEADER_TEMPLATE = "<%s>; rel=%s";

    public static final Pattern AO_LINK_HEADER_PATTERN = Pattern
            .compile("<(.+)>; rel=\"http://purl.org/ao/annotates\"");

    public static final String AO_ANNOTATION_BODY_HEADER = "http://purl.org/ao/annotationBody";

    public static final String AO_ANNOTATES_RESOURCE_HEADER = "http://purl.org/ao/annotatesResource";
}
