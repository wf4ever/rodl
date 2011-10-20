package pl.psnc.dl.wf4ever;

/**
 * 
 * @author nowakm
 *
 */
public final class Constants
{

	/**
	 * No instances allowed
	 */
	private Constants()
	{
		// nop
	}

	public final static String ACCESSTOKEN_URL_PART = "accesstoken";

	public final static String CLIENTS_URL_PART = "clients";

	public final static String USERS_URL_PART = "users";

	public final static String WORKSPACES_URL_PART = "workspaces";

	public final static String RESEARCH_OBJECTS_URL_PART = "ROs";

	public final static String MANIFEST_FILENAME = "manifest.rdf";

	public final static String CONTENT_DISPOSITION_HEADER_NAME = "Content-Disposition";

	public final static String CONTENT_TYPE_HEADER_NAME = "Content-Type";

	public static final String RDF_XML_MIME_TYPE = "application/rdf+xml";

	public static final long EDITION_QUERY_PARAM_DEFAULT = 0L;

	public static final String EDITION_QUERY_PARAM_DEFAULT_STRING = "0";

	public static final String OAUTH_MANAGER = "oauthManager";

	/**
	 * Used for accessing DLibraDataSource stored in HttpRequest
	 */
	public static final String DLFACTORY = "dlFactory";

}
