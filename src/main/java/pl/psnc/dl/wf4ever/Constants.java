package pl.psnc.dl.wf4ever;

import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

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
    public static final String LINK_HEADER = "Link";

    /** See ROSR API. */
    public static final String LINK_HEADER_TEMPLATE = "<%s>; rel=\"%s\"";

    /** See ROSR API. */
    public static final Pattern AO_LINK_HEADER_PATTERN = Pattern
            .compile("\\s*<([^>]*)>\\s*;\\s*rel\\s*=\\s*\"http://purl.org/ao/annotatesResource\"");

    /** See ROSR API. */
    public static final String AO_ANNOTATION_BODY_HEADER = "http://purl.org/ao/body";

    /** See ROSR API. */
    public static final String AO_ANNOTATES_RESOURCE_HEADER = "http://purl.org/ao/annotatesResource";

    /** See ROSR API. */
    public static final Resource ORE_PROXY_CLASS = ModelFactory.createDefaultModel().createResource(
        "http://www.openarchives.org/ore/terms/Proxy");

    /** See ROSR API. */
    public static final Property ORE_PROXY_FOR_PROPERTY = ModelFactory.createDefaultModel().createProperty(
        "http://www.openarchives.org/ore/terms/proxyFor");

    /** ro:AggregatedAnnotation. */
    public static final Resource RO_AGGREGATED_ANNOTATION_CLASS = ModelFactory.createDefaultModel().createResource(
        "http://purl.org/wf4ever/ro#AggregatedAnnotation");

    /** ao:annotatesResource. */
    public static final Property AO_ANNOTATES_RESOURCE_PROPERTY = ModelFactory.createDefaultModel().createProperty(
        "http://purl.org/ao/annotatesResource");

    /** body. */
    public static final Property AO_BODY_PROPERTY = ModelFactory.createDefaultModel().createProperty(
        "http://purl.org/ao/body");
}
