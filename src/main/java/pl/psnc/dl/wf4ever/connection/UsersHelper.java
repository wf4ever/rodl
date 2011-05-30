/**
 * 
 */
package pl.psnc.dl.wf4ever.connection;

import java.rmi.RemoteException;
import java.util.Arrays;

import pl.psnc.dlibra.metadata.Directory;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.DirectoryManager;
import pl.psnc.dlibra.metadata.Language;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.mgmt.DLStaticServiceResolver;
import pl.psnc.dlibra.mgmt.UserServiceResolver;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.DuplicatedValueException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.user.Actor;
import pl.psnc.dlibra.user.ActorId;
import pl.psnc.dlibra.user.DirectoryRightId;
import pl.psnc.dlibra.user.RightOperation;
import pl.psnc.dlibra.user.User;
import pl.psnc.dlibra.user.UserId;
import pl.psnc.dlibra.user.UserManager;
import pl.psnc.dlibra.user.UserServer;

/**
 * @author piotrhol
 *
 */
public class UsersHelper
{

	private DLibraDataSource dLibra;

	private DirectoryManager directoryManager;

	private UserServiceResolver serviceResolver;

	private UserManager userManager;


	public UsersHelper(DLibraDataSource dLibraDataSource)
		throws RemoteException
	{
		this.dLibra = dLibraDataSource;

		directoryManager = dLibraDataSource.getMetadataServer()
				.getDirectoryManager();
		serviceResolver = dLibraDataSource.getServiceResolver();
		userManager = dLibraDataSource.getUserManager();
	}


	/**
	* Creates user in dLibra, equivalent to workspace in ROSRS.
	* @param login
	* @param password
	* @throws RemoteException
	* @throws DLibraException
	*/
	public void createUser(String login, String password)
		throws RemoteException, DLibraException
	{
		// check if user already exists
		try {
			userManager.getUserData(login);
			throw new DuplicatedValueException(null, "User already exists",
					login);
		}
		catch (IdNotFoundException e) {
			// ok - user does not exist
		}

		DirectoryId workspaceDir = createDirectory(login);

		User user = new User(login);
		user.setPassword(password);
		user.setHomedir(workspaceDir);
		user.setType(Actor.USER);
		user.setLogin(login);
		user.setEmail(login);

		UserId userId = userManager.createUser(user);

		UserServer userServer = DLStaticServiceResolver.getUserServer(
			serviceResolver, null);
		userServer.getRightManager().setDirectoryRights(
			workspaceDir,
			Arrays.asList((ActorId) userId),
			new RightOperation(DirectoryRightId.PUBLICATION_MGMT,
					RightOperation.ADD));
	}


	private DirectoryId createDirectory(String name)
		throws RemoteException, DLibraException
	{
		MetadataServer metadataServer = DLStaticServiceResolver
				.getMetadataServer(serviceResolver, null);
		Directory directory = new Directory(null, new DirectoryId(
				dLibra.getWorkspacesDir()));
		for (String language : metadataServer.getLanguageManager()
				.getLanguageNames(Language.LAN_INTERFACE)) {
			directory.setLanguageName(language);
			directory.setName(name);
		}
		return directoryManager.createDirectory(directory);
	}


	/**
	* Deletes user from dLibra, equivalent to workspace in ROSRS.
	* @param login
	* @throws RemoteException
	* @throws DLibraException
	*/
	public void deleteUser(String login)
		throws RemoteException, DLibraException
	{
		// TODO do we really want to permanently remove the user and its
		// directory?

		User userData = userManager.getUserData(login);

		directoryManager.removeDirectory(userData.getHomedir(), true,
			"Workspace removed from RO SRS");

		userManager.removeUser(userData.getId());
	}

}
