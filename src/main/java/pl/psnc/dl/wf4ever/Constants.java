package pl.psnc.dl.wf4ever;

import java.util.regex.Pattern;

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


    /** dLibra user request cycle key. */
    public static final String USER = "User";

    /** workspace in dLibra. */
    public static final String WORKSPACE_ID = "default";

    /** version in dLibra. */
    public static final String VERSION_ID = "v1";

    /** HTTP Slug header. */
    public static final String SLUG_HEADER = "Slug";

    /** See ROSR API. */
    public static final String AO_ANNOTATES_HEADER = "http://purl.org/ao/annotates";

    /** See ROSR API. */
    public static final String ORE_PROXY_FOR_HEADER = "http://www.openarchives.org/ore/terms/proxyFor";

    /** See ROSR API. */
    public static final String PROXY_MIME_TYPE = "application/vnd.wf4ever.proxy";

    /** See ROSR API. */
    public static final String ANNOTATION_MIME_TYPE = "application/vnd.wf4ever.annotation";

    /** See ROSR API. */
    public static final String ACCEPT_HEADER = "Accept";

    /** See ROSR API. */
    public static final String MANIFEST_PATH = ".ro/manifest.rdf";

    /** See ROSR API. */
    public static final String LINK_HEADER = "Link";

    /** See ROSR API. */
    public static final String LINK_HEADER_TEMPLATE = "<%s>; rel=\"%s\"";

    /** See ROSR API. */
    public static final Pattern AO_LINK_HEADER_PATTERN = Pattern
            .compile("\\s*<([^>]*)>\\s*;\\s*rel\\s*=\\s*\"http://purl.org/ao/annotatesResource\"");
}
