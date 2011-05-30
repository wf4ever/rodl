package pl.psnc.dl.wf4ever.connection;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.mgmt.DLStaticServiceResolver;
import pl.psnc.dlibra.mgmt.UserServiceResolver;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.user.UserManager;

/**
 * 
 * @author nowakm, piotrhol
 * 
 */
public class DLibraDataSource
{

	@SuppressWarnings("unused")
	private final static Logger logger = Logger
			.getLogger(DLibraDataSource.class);

	public final static int BUFFER_SIZE = 4096;

	private UserServiceResolver serviceResolver;

	private String userLogin;

	private ContentServer contentServer;

	private UserManager userManager;

	private MetadataServer metadataServer;

	private UsersHelper usersHelper;

	private PublicationsHelper publicationsHelper;

	private FilesHelper filesHelper;

	private ManifestHelper manifestHelper;

	private AttributesHelper attributesHelper;

	private long workspacesDir;


	public DLibraDataSource(UserServiceResolver userServiceResolver,
			String userLogin, long workspacesDirectoryId)
		throws RemoteException, DLibraException
	{
		this.serviceResolver = userServiceResolver;
		this.userLogin = userLogin;
		this.workspacesDir = workspacesDirectoryId;

		metadataServer = DLStaticServiceResolver.getMetadataServer(
			serviceResolver, null);

		contentServer = DLStaticServiceResolver.getContentServer(
			serviceResolver, null);

		userManager = DLStaticServiceResolver.getUserServer(serviceResolver,
			null).getUserManager();

		usersHelper = new UsersHelper(this);
		publicationsHelper = new PublicationsHelper(this);
		filesHelper = new FilesHelper(this);
		manifestHelper = new ManifestHelper(this);
		attributesHelper = new AttributesHelper(this);
	}


	UserServiceResolver getServiceResolver()
	{
		return serviceResolver;
	}


	ContentServer getContentServer()
	{
		return contentServer;
	}


	UserManager getUserManager()
	{
		return userManager;
	}


	MetadataServer getMetadataServer()
	{
		return metadataServer;
	}


	public UsersHelper getUsersHelper()
	{
		return usersHelper;
	}


	public PublicationsHelper getPublicationsHelper()
	{
		return publicationsHelper;
	}


	public FilesHelper getFilesHelper()
	{
		return filesHelper;
	}


	public ManifestHelper getManifestHelper()
	{
		return manifestHelper;
	}


	public AttributesHelper getAttributesHelper()
	{
		return attributesHelper;
	}


	String getUserLogin()
	{
		return userLogin;
	}

	long getWorkspacesDir()
	{
		return workspacesDir;
	}


}
