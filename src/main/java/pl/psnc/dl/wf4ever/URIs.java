package pl.psnc.dl.wf4ever;

import pl.psnc.dl.wf4ever.sms.SemanticMetadataService.Notation;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * 
 * @author Piotr Hołubowicz
 * 
 */
public final class URIs {

	/**
	 * No instances allowed
	 */
	private URIs() {
		// nop
	}

	public final static String WORKSPACES = "workspaces";

	public final static String WORKSPACE_ID = WORKSPACES + "/{W_ID}";

	public final static String ROS = WORKSPACE_ID + "/ROs";

	public final static String RO_ID = ROS + "/{RO_ID}";

	public final static String VERSION_ID = RO_ID + "/{RO_VERSION_ID}";

	public final static String FILE = VERSION_ID + "/{FILE_PATH : [\\\\w\\\\d:#%/;$()~_?\\\\-=\\\\\\\\.&]+}";

	public final static String METADATA = VERSION_ID + "/.ro_metadata";

	public final static String MANIFEST = METADATA + "/manifest";

	public final static String ANNOTATIONS = METADATA + "/annotations";

	public final static String ANNOTATION_ID = ANNOTATIONS + "/{A_ID}";

	public final static String USERS = "users";

	public final static String USER_ID = USERS + "/{U_ID}";

	public final static String ACCESSTOKENS = "accesstoken";

	public final static String ACCESSTOKEN_ID = ACCESSTOKENS + "/{T_ID}";

	public final static String CLIENTS = "clients";

	public final static String CLIENT_ID = CLIENTS + "/{C_ID}";

	public static ContentDisposition generateContentDisposition(Notation notation, String filename) {
		String contentType = null;
		String extension = null;
		switch (notation) {
		case RDF_XML:
			contentType = "application/rdf+xml";
			extension = ".rdf";
			break;
		case TRIG:
			contentType = "application/x+trig";
			extension = ".trig";
			break;
		case TEXT_PLAIN:
			contentType = "text/plain";
			extension = ".txt";
			break;
		}
		return ContentDisposition.type(contentType).fileName(filename + "." + extension).build();
	}

	/**
	 * Returns the notation based on the MIME content type. If content type is
	 * null or it is not recognized, RDF_XML is returned.
	 * 
	 * @param contentType MIME content type
	 * @return
	 */
	public static Notation recognizeNotation(String contentType) {
		if ("application/x+trig".equals(contentType))
			return Notation.TRIG;
		if ("text/plain".equals(contentType))
			return Notation.TEXT_PLAIN;
		return Notation.RDF_XML;
	}
}
