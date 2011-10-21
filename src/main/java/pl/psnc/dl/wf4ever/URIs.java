package pl.psnc.dl.wf4ever;

/**
 * 
 * @author nowakm
 *
 */
public final class URIs
{

	/**
	 * No instances allowed
	 */
	private URIs()
	{
		// nop
	}

	public final static String WORKSPACES = "workspaces";

	public final static String WORKSPACE_ID = WORKSPACES + "/{W_ID}";

	public final static String ROS = WORKSPACE_ID + "/ROs";

	public final static String RO_ID = ROS + "/{RO_ID}";

	public final static String VERSION_ID = RO_ID + "/{RO_VERSION_ID}";

	public final static String FILE = VERSION_ID
			+ "/{FILE_PATH : [\\\\w\\\\d:#%/;$()~_?\\\\-=\\\\\\\\.&]+}";

	public final static String ANNOTATIONS = VERSION_ID + "/annotations";

	public final static String ANNOTATION_ID = ANNOTATIONS + "/{A_ID}";

	public final static String PROXY_ID = VERSION_ID + "/proxies/{P_ID}";

	public final static String USERS = "users";

	public final static String USER_ID = USERS + "/{U_ID}";

	public final static String ACCESSTOKENS = "accesstoken";

	public final static String ACCESSTOKEN_ID = ACCESSTOKENS + "/{A_ID}";

	public final static String CLIENTS = "clients";

	public final static String CLIENT_ID = CLIENTS + "/{C_ID}";
}
