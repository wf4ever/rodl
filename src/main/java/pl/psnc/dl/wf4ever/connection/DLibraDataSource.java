package pl.psnc.dl.wf4ever.connection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.EmptyFoldersUtility;
import pl.psnc.dl.wf4ever.RdfBuilder;
import pl.psnc.dlibra.common.Id;
import pl.psnc.dlibra.common.Info;
import pl.psnc.dlibra.common.InputFilter;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.Directory;
import pl.psnc.dlibra.metadata.DirectoryFilter;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.DirectoryManager;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.ElementInfo;
import pl.psnc.dlibra.metadata.File;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.FileId;
import pl.psnc.dlibra.metadata.FileInfo;
import pl.psnc.dlibra.metadata.FileManager;
import pl.psnc.dlibra.metadata.GroupPublicationInfo;
import pl.psnc.dlibra.metadata.Language;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationInfo;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.Version;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.metadata.VersionInfo;
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
import pl.psnc.util.IOUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.DCTerms;

/**
 * 
 * @author nowakm, piotrhol
 * 
 */
public class DLibraDataSource implements DLibraDataSourceInterface
{

	private final static Logger logger = Logger
			.getLogger(DLibraDataSource.class);

	private static final int BUFFER_SIZE = 4096;

	final UserServiceResolver serviceResolver;

	final String userLogin;

	final long workspacesDir;

	final PublicationManager publicationManager;

	final FileManager fileManager;

	final DirectoryManager directoryManager;

	final ContentServer contentServer;

	final UserManager userManager;


	public DLibraDataSource(UserServiceResolver userServiceResolver,
			String userLogin, long workspacesDirectoryId)
		throws RemoteException, DLibraException
	{
		this.serviceResolver = userServiceResolver;
		this.userLogin = userLogin;
		this.workspacesDir = workspacesDirectoryId;

		MetadataServer metadataServer = DLStaticServiceResolver
				.getMetadataServer(serviceResolver, null);
		publicationManager = metadataServer.getPublicationManager();
		fileManager = metadataServer.getFileManager();
		directoryManager = metadataServer.getDirectoryManager();

		contentServer = DLStaticServiceResolver.getContentServer(
			serviceResolver, null);

		userManager = DLStaticServiceResolver.getUserServer(serviceResolver,
			null).getUserManager();
	}


	// user == workspace
	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#createUser(java.lang.String, java.lang.String)
	 */
	@Override
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
		Directory directory = new Directory(null,
				new DirectoryId(workspacesDir));
		for (String language : metadataServer.getLanguageManager()
				.getLanguageNames(Language.LAN_INTERFACE)) {
			directory.setLanguageName(language);
			directory.setName(name);
		}
		return directoryManager.createDirectory(directory);
	}


	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#deleteUser(java.lang.String)
	 */
	@Override
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


	// user group publications == research objects in workspace
	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#listUserGroupPublications()
	 */
	@Override
	public List<GroupPublicationInfo> listUserGroupPublications()
		throws RemoteException, DLibraException
	{
		DirectoryId workspaceDir = getWorkspaceDir();
		Collection<Info> resultInfos = directoryManager
				.getObjects(
					new DirectoryFilter(null, workspaceDir)
							.setGroupStatus(Publication.PUB_GROUP_ROOT)
							.setState(
								(byte) (Publication.PUB_STATE_ALL - Publication.PUB_STATE_PERMANENT_DELETED)),
					new OutputFilter(ElementInfo.class, List.class))
				.getResultInfos();

		ArrayList<GroupPublicationInfo> result = new ArrayList<GroupPublicationInfo>();
		for (Info info : resultInfos) {
			if (info instanceof GroupPublicationInfo) {
				result.add((GroupPublicationInfo) info);
			}
		}
		return result;
	}


	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#createGroupPublication(java.lang.String)
	 */
	@Override
	public void createGroupPublication(String groupPublicationName)
		throws RemoteException, DLibraException
	{
		DirectoryId parent = getWorkspaceDir();
		try {
			getGroupId(groupPublicationName);
			throw new DuplicatedValueException(null, "RO already exists",
					groupPublicationName);
		}
		catch (IdNotFoundException e) {
			// OK - group does not exist
		}

		Publication publication = new Publication(parent);
		publication.setGroupStatus(Publication.PUB_GROUP_ROOT);
		publication.setName(groupPublicationName);

		publicationManager.createPublication(publication);
	}


	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#deleteGroupPublication(java.lang.String)
	 */
	@Override
	public void deleteGroupPublication(String groupPublicationName)
		throws RemoteException, DLibraException
	{
		PublicationId groupId = getGroupId(groupPublicationName);
		publicationManager.removePublication(groupId, true,
			"Research object removed");
	}


	// publication in group == version of research object
	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#listPublicationsInGroup(java.lang.String)
	 */
	@Override
	public List<PublicationInfo> listPublicationsInGroup(String groupPublicationName)
		throws RemoteException, DLibraException
	{
		PublicationId groupId = getGroupId(groupPublicationName);

		Collection<Info> resultInfos = publicationManager
				.getObjects(
					new PublicationFilter(null, groupId)
							.setGroupStatus(Publication.PUB_GROUP_LEAF)
							.setPublicationState(
								(byte) (Publication.PUB_STATE_ALL - Publication.PUB_STATE_PERMANENT_DELETED)),
					new OutputFilter(AbstractPublicationInfo.class, List.class))
				.getResultInfos();

		ArrayList<PublicationInfo> result = new ArrayList<PublicationInfo>();
		for (Info info : resultInfos) {
			result.add((PublicationInfo) info);
		}
		return result;
	}


	private PublicationId getGroupId(String groupPublicationName)
		throws RemoteException, DLibraException
	{
		Collection<Info> resultInfos = directoryManager
				.getObjects(
					new DirectoryFilter(null, getWorkspaceDir())
							.setGroupStatus(Publication.PUB_GROUP_ROOT)
							.setState(
								(byte) (Publication.PUB_STATE_ALL - Publication.PUB_STATE_PERMANENT_DELETED)),
					new OutputFilter(ElementInfo.class, List.class))
				.getResultInfos();
		for (Info info : resultInfos) {
			if (info instanceof GroupPublicationInfo
					&& info.getLabel().equals(groupPublicationName)) {
				return (PublicationId) info.getId();
			}
		}
		throw new IdNotFoundException(groupPublicationName);
	}


	/**
	 * Returns list of URIs of files in publication
	 */
	private List<String> getFilePathsInPublication(String groupPublicationName,
			String publicationName)
		throws RemoteException, DLibraException
	{
		return getFilePathsInFolder(groupPublicationName, publicationName, null);
	}


	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#getFilePathsInFolder(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<String> getFilePathsInFolder(String groupPublicationName,
			String publicationName, String folder)
		throws RemoteException, DLibraException
	{
		ArrayList<String> result = new ArrayList<String>();
		for (FileInfo fileInfo : getFilesInFolder(groupPublicationName, publicationName,
			folder).values()) {
			if (EmptyFoldersUtility.isDlibraPath(fileInfo.getFullPath())) {
				result.add(EmptyFoldersUtility.convertDlibra2Real(fileInfo
						.getFullPath()));
			}
			else {
				result.add(fileInfo.getFullPath());
			}
		}
		return result;
	}


	/**
	 * 
	 * @param groupPublicationName
	 * @param publicationName
	 * @param folder
	 * @return FileInfo will have paths starting with "/"
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	private Map<VersionId, FileInfo> getFilesInFolder(String groupPublicationName,
			String publicationName, String folder)
		throws RemoteException, DLibraException
	{
		PublicationId publicationId = getPublicationId(getGroupId(groupPublicationName),
			publicationName);
		EditionId editionId = getEditionId(publicationId);

		Map<VersionId, FileInfo> result = new HashMap<VersionId, FileInfo>();
		if (folder != null && !folder.endsWith("/"))
			folder = folder.concat("/");

		List<Id> versionIds = (List<Id>) publicationManager.getObjects(
			new EditionFilter(editionId), new OutputFilter(VersionId.class))
				.getResultIds();
		List<Info> fileInfos = (List<Info>) fileManager.getObjects(
			new InputFilter(new ArrayList<Id>(versionIds)),
			new OutputFilter(FileInfo.class)).getResultInfos();
		if (versionIds.size() != fileInfos.size()) {
			logger.error("Version ids size is not equal to file infos size");
		}
		for (int i = 0; i < versionIds.size(); i++) {
			VersionId versionId = (VersionId) versionIds.get(i);
			FileInfo fileInfo = (FileInfo) fileInfos.get(i);
			String filePath = fileInfo.getFullPath();
			logger.debug("File path is " + filePath + ".");
			if (EmptyFoldersUtility.isDlibraPath(filePath) &&
				EmptyFoldersUtility.convertDlibra2Real(filePath).equals("/" + folder)) {
				// empty folder
				result.clear();
				return result;
			}
			if (!filePath.equals("/" + Constants.MANIFEST_FILENAME)) {
				if (folder == null || filePath.startsWith("/" + folder)) {
					logger.debug("File " + fileInfo.getFullPath()
							+ " is inside " + folder);
					result.put(versionId, fileInfo);
				}
			}
		}

		if (folder != null && result.isEmpty()) {
			throw new IdNotFoundException(folder);
		}

		return result;
	}


	private PublicationId getPublicationId(PublicationId groupId,
			String publicationName)
		throws RemoteException, DLibraException
	{
		Collection<Info> resultInfos = publicationManager
				.getObjects(
					new PublicationFilter(null, groupId)
							.setGroupStatus(Publication.PUB_GROUP_LEAF)
							.setPublicationState(
								(byte) (Publication.PUB_STATE_ALL - Publication.PUB_STATE_PERMANENT_DELETED)),
					new OutputFilter(AbstractPublicationInfo.class))
				.getResultInfos();
		for (Info info : resultInfos) {
			if (info.getLabel().equals(publicationName)) {
				return (PublicationId) info.getId();
			}
		}
		throw new IdNotFoundException(publicationName);
	}


	private EditionId getEditionId(PublicationId publicationId)
		throws RemoteException, DLibraException
	{
		Collection<Id> resultIds = publicationManager
				.getObjects(
					new PublicationFilter(null, publicationId).setEditionState(Edition.ALL_STATES
							- Edition.PERMANENT_DELETED),
					new OutputFilter(EditionId.class)).getResultIds();
		if (resultIds.size() != 1) {
			throw new DLibraException(null, "Invalid state of publication "
					+ publicationId + ": " + resultIds.size() + " editions.") {

				private static final long serialVersionUID = -7493352685629908419L;
				// TODO probably another exception would fit better here
			};
		}
		return (EditionId) resultIds.iterator().next();
	}


	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#getZippedPublication(java.lang.String, java.lang.String)
	 */
	@Override
	public InputStream getZippedPublication(String groupPublicationName,
			String publicationName)
		throws RemoteException, DLibraException
	{
		return getZippedFolder(groupPublicationName, publicationName, null);
	}


	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#getZippedFolder(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public InputStream getZippedFolder(String groupPublicationName,
			String publicationName, String folderNotStandardized)
		throws RemoteException, DLibraException
	{
		final String folder = (folderNotStandardized == null ? null
				: (folderNotStandardized.endsWith("/") ? folderNotStandardized
						: folderNotStandardized.concat("/")));
		final Map<VersionId, FileInfo> fileVersionsAndInfos = getFilesInFolder(
			groupPublicationName, publicationName, folder);

		PipedInputStream in = new PipedInputStream();
		final PipedOutputStream out;
		try {
			out = new PipedOutputStream(in);
		}
		catch (IOException e) {
			throw new RuntimeException("This should never happen", e);
		}

		final ZipOutputStream zipOut = new ZipOutputStream(out);

		new Thread("publication zip downloader (" + publicationName + ")") {

			public void run()
			{
				try {
					for (Map.Entry<VersionId, FileInfo> mapEntry : fileVersionsAndInfos
							.entrySet()) {
						VersionId versionId = mapEntry.getKey();
						String filePath = mapEntry.getValue().getFullPath()
								.substring(1);
						ZipEntry entry = new ZipEntry(filePath);
						zipOut.putNextEntry(entry);
						InputStream versionInputStream = contentServer
								.getVersionInputStream(versionId);
						IOUtils.copyStream(versionInputStream, zipOut);
						versionInputStream.close();
					}
				}
				catch (IOException e) {
					logger.error("Zip transmission failed", e);
				}
				catch (DLibraException e) {
					logger.error("Zip transmission failed", e);
				}
				finally {
					try {
						zipOut.close();
					}
					catch (Exception e) {
						logger.warn("Could not close the ZIP file: "
								+ e.getMessage());
						try {
							out.close();
						}
						catch (IOException e1) {
							logger.error(
								"Could not close the ZIP output stream", e1);
						}
					}
				}
			};
		}.start();
		return in;
	}


	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#createPublication(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void createPublication(String groupPublicationName,
			String publicationName, String basePublicationName,
			String manifestUri)
		throws DLibraException, IOException, TransformerException
	{
		PublicationId groupId = getGroupId(groupPublicationName);
		try {
			getPublicationId(groupId, publicationName);
			throw new DuplicatedValueException(null,
					"RO Version already exists", groupPublicationName + "/"
							+ publicationName);
		}
		catch (IdNotFoundException e) {
			// OK - the publication does not exist
		}

		Publication publication = new Publication(null, getWorkspaceDir());
		publication.setParentPublicationId(groupId);
		publication.setName(publicationName);
		publication.setPosition(0);
		publication.setGroupStatus(Publication.PUB_GROUP_LEAF);
		publication.setSecured(false);
		publication.setState(Publication.PUB_STATE_ACTUAL);
		if (basePublicationName != null && !basePublicationName.isEmpty()) {
			PublicationId basePublicationId = getPublicationId(groupId,
				basePublicationName);

			PublicationId publicationId = publicationManager
					.createPublication(publication);
			VersionId[] copyVersions = copyVersions(basePublicationId,
				publicationId);
			Edition edition = new Edition(null, publicationId, false);
			edition.setName(publicationName);
			publicationManager.createEdition(edition, copyVersions);

		}
		else {
			PublicationId publicationId = publicationManager
					.createPublication(publication);

			File file = new File("application/rdf+xml", publicationId, "/"
					+ Constants.MANIFEST_FILENAME);

			Version createdVersion = fileManager.createVersion(file, 0,
				new Date(), "");

			String manifest = createManifest(manifestUri);

			OutputStream output = contentServer
					.getVersionOutputStream(createdVersion.getId());
			output.write(manifest.getBytes());
			output.close();

			publicationManager.setMainFile(publicationId,
				createdVersion.getFileId());

			Edition edition = new Edition(null, publicationId, false);
			edition.setName(publicationName);
			publicationManager.createEdition(edition,
				new VersionId[] { createdVersion.getId()});
		}

		// add hasVersion tag to all versions
		{
			List<PublicationInfo> list = listPublicationsInGroup(groupPublicationName);
			for (PublicationInfo p : list) {
				try {
					regenerateManifest(groupPublicationName, p.getLabel(),
						manifestUri,
						getManifest(groupPublicationName, p.getLabel()));
				}
				catch (JenaException e) {
					logger.warn("Manifest stored for publication "
							+ groupPublicationName + " is malformed");
				}
			}
		}
	}


	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#deletePublication(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void deletePublication(String groupPublicationName,
			String publicationName, String versionUri)
		throws DLibraException, IOException, TransformerException
	{
		PublicationId publicationId = getPublicationId(
			getGroupId(groupPublicationName), publicationName);

		publicationManager.removePublication(publicationId, true,
			"Research Object Version removed.");

		{
			List<PublicationInfo> list = listPublicationsInGroup(groupPublicationName);
			for (PublicationInfo p : list) {
				try {
					regenerateManifest(groupPublicationName, p.getLabel(),
						versionUri,
						getManifest(groupPublicationName, p.getLabel()));
				}
				catch (JenaException e) {
					logger.warn("Manifest stored for publication "
							+ groupPublicationName + " is malformed");
				}
			}
		}
	}


	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#getManifest(java.lang.String, java.lang.String)
	 */
	@Override
	public String getManifest(String groupPublicationName,
			String publicationName)
		throws IOException, DLibraException
	{
		InputStream fileContents = getFileContents(groupPublicationName,
			publicationName, Constants.MANIFEST_FILENAME);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = 0;
			while ((bytesRead = fileContents.read(buffer)) > 0) {
				output.write(buffer, 0, bytesRead);
			}
		}
		finally {
			fileContents.close();
		}
		return new String(output.toByteArray());
	}


	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#updateManifest(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void updateManifest(String versionUri, String groupPublicationName,
			String publicationName, String manifest)
		throws DLibraException, IOException, TransformerException,
		JenaException
	{

		regenerateManifest(groupPublicationName, publicationName, versionUri,
			manifest);

	}


	private String createManifest(String uri)
		throws TransformerException
	{
		Resource resource = RdfBuilder.createResource(uri);

		resource.addLiteral(DCTerms.identifier, "");
		resource.addLiteral(DCTerms.creator, "");
		resource.addLiteral(DCTerms.title, "");
		resource.addLiteral(DCTerms.description, "");
		resource.addLiteral(RdfBuilder.OXDS_CURRENT_VERSION,
			uri.substring(1 + uri.lastIndexOf("/")));
		resource.addProperty(DCTerms.created,
			RdfBuilder.createDateLiteral(new Date()));
		resource.addLiteral(DCTerms.source, "");
		return RdfBuilder.serializeResource(resource);
	}


	private void regenerateManifest(String groupPublicationName,
			String publicationName, String versionUri, String baseManifest)
		throws DLibraException, IOException, TransformerException,
		JenaException
	{

		List<String> list = getFilePathsInPublication(groupPublicationName,
			publicationName);

		for (int i = 0; i < list.size(); i++) {
			list.set(i, versionUri + list.get(i));
		}

		Resource resource = RdfBuilder.createCollection(versionUri, list);

		// add read only tags
		{
			String researchObjectUri = versionUri.substring(0,
				versionUri.lastIndexOf("/"));

			for (PublicationInfo info : listPublicationsInGroup(groupPublicationName)) {
				resource.addProperty(DCTerms.hasVersion, researchObjectUri
						+ "/" + info.getLabel());
			}

			resource.addProperty(RdfBuilder.OXDS_CURRENT_VERSION,
				publicationName);
			resource.addProperty(DCTerms.modified,
				RdfBuilder.createDateLiteral(new Date()));
		}

		// read manifest and copy user-editable tags
		{
			Model model = ModelFactory.createDefaultModel();
			model.read(new ByteArrayInputStream(baseManifest.getBytes()), null);

			StmtIterator iterator = model.listStatements();
			for (Statement statement : iterator.toList()) {
				if (!RdfBuilder.READ_ONLY_PROPERTIES.contains(statement
						.getPredicate())) {
					resource.addProperty(statement.getPredicate(),
						statement.getObject());
				}
			}
		}

		InputStream is = new ByteArrayInputStream(RdfBuilder.transformManifest(
			RdfBuilder.serializeResource(resource)).getBytes());
		// save manifest.rdf
		createOrUpdateFile(versionUri, groupPublicationName, publicationName,
			Constants.MANIFEST_FILENAME, is, Constants.RDF_XML_MIME_TYPE, false);
	}


	// maybe we should merge getFileContents and getFileMimeType in one method?
	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#getFileContents(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public InputStream getFileContents(String groupPublicationName,
			String publicationName, String filePath)
		throws IOException, DLibraException
	{
		PublicationId publicationId = getPublicationId(
			getGroupId(groupPublicationName), publicationName);
		EditionId editionId = getEditionId(publicationId);
		VersionId versionId = getVersionId(editionId, filePath);

		InputStream versionInputStream = contentServer
				.getVersionInputStream(versionId);
		return versionInputStream;
	}


	private VersionId getVersionId(EditionId editionId, String filePath)
		throws IdNotFoundException, RemoteException, DLibraException
	{
		VersionId versionId = (VersionId) fileManager.getObjects(
			new FileFilter().setEditionId(editionId)
					.setFileName("/" + filePath),
			new OutputFilter(VersionId.class)).getResultId();
		return versionId;
	}


	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#getFileMimeType(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getFileMimeType(String groupPublicationName,
			String publicationName, String filePath)
		throws RemoteException, DLibraException
	{
		PublicationId publicationId = getPublicationId(
			getGroupId(groupPublicationName), publicationName);
		EditionId editionId = getEditionId(publicationId);
		VersionId versionId = getVersionId(editionId, filePath);
		VersionInfo versionInfo = (VersionInfo) fileManager.getObjects(
			new InputFilter(versionId), new OutputFilter(VersionInfo.class))
				.getResultInfo();
		FileInfo fileInfo = (FileInfo) fileManager.getObjects(
			new FileFilter(versionInfo.getFileId()),
			new OutputFilter(FileInfo.class)).getResultInfo();

		return fileInfo.getMimeType();
	}


	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#getFileMetadata(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getFileMetadata(String groupPublicationName,
			String publicationName, String filePath, String fullPath)
		throws RemoteException, DLibraException, TransformerException
	{
		PublicationId publicationId = getPublicationId(
			getGroupId(groupPublicationName), publicationName);
		EditionId editionId = getEditionId(publicationId);
		VersionId versionId = getVersionId(editionId, filePath);
		VersionInfo versionInfo = (VersionInfo) fileManager.getObjects(
			new InputFilter(versionId), new OutputFilter(VersionInfo.class))
				.getResultInfo();
		FileInfo fileInfo = (FileInfo) fileManager.getObjects(
			new FileFilter(versionInfo.getFileId()),
			new OutputFilter(FileInfo.class)).getResultInfo();

		byte[] fileDigest = contentServer.getFileDigest(versionId);
		String digest = getHex(fileDigest);
		Resource resource = RdfBuilder.createResource(fullPath);

		resource.addProperty(DCTerms.modified,
			RdfBuilder.createDateLiteral(versionInfo.getLastModificationDate()));

		resource.addLiteral(DCTerms.identifier, "MD5: " + digest);
		resource.addLiteral(DCTerms.type, fileInfo.getMimeType());

		resource.addLiteral(DCTerms.extent, versionInfo.getSize());

		return RdfBuilder.serializeResource(resource);
	}


	/*
	 * from http://rgagnon.com/javadetails/java-0596.html
	 */
	private String getHex(byte[] raw)
	{
		final String HEXES = "0123456789ABCDEF";
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (final byte b : raw) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(
				HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}


	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#createOrUpdateFile(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)
	 */
	@Override
	public void createOrUpdateFile(String versionUri,
			String groupPublicationName, String publicationName,
			String filePath, InputStream inputStream, String mimeType)
		throws IOException, DLibraException, TransformerException
	{
		createOrUpdateFile(versionUri, groupPublicationName, publicationName,
			filePath, inputStream, mimeType, true);
	}


	private void createOrUpdateFile(String versionUri,
			String groupPublicationName, String publicationName,
			String filePath, InputStream inputStream, String mimeType,
			boolean generateManifest)
		throws IOException, DLibraException, TransformerException
	{
		PublicationId publicationId = getPublicationId(
			getGroupId(groupPublicationName), publicationName);
		EditionId editionId = getEditionId(publicationId);

		if (filePath.endsWith("/")) {
			// slash at the end means empty folder
			logger.debug("Slash at the end, file " + filePath
					+ " will be an empty folder");
			filePath = EmptyFoldersUtility.convertReal2Dlibra(filePath);
		}

		File file;
		VersionId versionId;
		try {
			versionId = getVersionId(editionId, filePath);
			VersionInfo versionInfo = (VersionInfo) fileManager
					.getObjects(new InputFilter(versionId),
						new OutputFilter(VersionInfo.class)).getResultInfo();
			file = (File) fileManager.getObjects(
				new FileFilter(versionInfo.getFileId()),
				new OutputFilter(File.class)).getResult();
		}
		catch (IdNotFoundException e) {
			versionId = null;
			file = new File(mimeType, publicationId, "/" + filePath);
		}
		Version createdVersion = fileManager.createVersion(file, 0, new Date(),
			"");

		OutputStream output = contentServer
				.getVersionOutputStream(createdVersion.getId());
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = 0;

			while ((bytesRead = inputStream.read(buffer)) > 0) {
				output.write(buffer, 0, bytesRead);
			}
		}
		finally {
			inputStream.close();
			output.close();
		}

		if (versionId != null) {
			fileManager.replaceVersion(versionId, createdVersion.getId());
		}
		else {
			Collection<Id> versionIds = publicationManager.getObjects(
				new EditionFilter(editionId),
				new OutputFilter(VersionId.class)).getResultIds();
			versionIds.add(createdVersion.getId());
			publicationManager.updateEditionVersions(editionId,
				versionIds.toArray(new VersionId[versionIds.size()]));
		}

		if (generateManifest) {
			try {
				regenerateManifest(groupPublicationName, publicationName,
					versionUri,
					getManifest(groupPublicationName, publicationName));
			}
			catch (JenaException e) {
				logger.warn("Manifest stored for publication "
						+ groupPublicationName + " is malformed");
			}
		}

		String intermediateFilePath = filePath;
		while (intermediateFilePath.lastIndexOf("/") > 0) {
			intermediateFilePath = intermediateFilePath.substring(0,
				intermediateFilePath.lastIndexOf("/"));
			try {
				deleteFile(versionUri, groupPublicationName, publicationName,
					EmptyFoldersUtility
							.convertReal2Dlibra(intermediateFilePath));
			}
			catch (IdNotFoundException ex) {
				// ok, this folder was not empty
			}
		}
	}


	private VersionId[] copyVersions(PublicationId sourcePublicationId,
			PublicationId targetPublicationId)
		throws IOException, DLibraException
	{
		EditionId sourceEditionId = getEditionId(sourcePublicationId);
		Collection<Id> sourceVersionIds = publicationManager.getObjects(
			new EditionFilter(sourceEditionId),
			new OutputFilter(VersionId.class)).getResultIds();
		Publication sourcePublication = (Publication) publicationManager
				.getObjects(new PublicationFilter(sourcePublicationId),
					new OutputFilter(Publication.class)).getResult();
		FileId mainFileId = null;
		VersionId sourceMainVersionId = (VersionId) fileManager
				.getObjects(
					new FileFilter(sourcePublication.getMainFileId())
							.setEditionId(sourceEditionId),
					new OutputFilter(VersionId.class)).getResultId();
		ArrayList<VersionId> copyVersionIds = new ArrayList<VersionId>();

		int i = 0;
		for (Id id : sourceVersionIds) {
			copyVersionIds
					.add(copyVersion((VersionId) id, targetPublicationId));
			if (id.equals(sourceMainVersionId)) {
				Version copyVersion = (Version) fileManager.getObjects(
					new InputFilter(
							copyVersionIds.get(copyVersionIds.size() - 1)),
					new OutputFilter(Version.class)).getResult();
				mainFileId = copyVersion.getFileId();
			}
			i++;
		}

		Publication targetPublication = (Publication) publicationManager
				.getObjects(new PublicationFilter(targetPublicationId),
					new OutputFilter(Publication.class)).getResult();
		targetPublication.setMainFileId(mainFileId);
		publicationManager.setPublicationData(targetPublication);
		return copyVersionIds.toArray(new VersionId[copyVersionIds.size()]);
	}


	private VersionId copyVersion(VersionId sourceVersionId,
			PublicationId targetPublicationId)
		throws IOException, DLibraException
	{
		VersionInfo versionInfo = (VersionInfo) fileManager.getObjects(
			new InputFilter(sourceVersionId),
			new OutputFilter(VersionInfo.class)).getResultInfo();
		File file = (File) fileManager.getObjects(
			new FileFilter(versionInfo.getFileId()),
			new OutputFilter(File.class)).getResult();
		File copiedFile = new File(file.getType(), targetPublicationId,
				file.getPath());
		Version newVersion = fileManager.createVersion(copiedFile, 0,
			new Date(), "");

		OutputStream output = contentServer.getVersionOutputStream(newVersion
				.getId());
		InputStream input = contentServer
				.getVersionInputStream(sourceVersionId);
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = 0;

			while ((bytesRead = input.read(buffer)) > 0) {
				output.write(buffer, 0, bytesRead);
			}
		}
		finally {
			input.close();
			output.close();
		}

		return newVersion.getId();
	}


	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface#deleteFile(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteFile(String versionUri, String groupPublicationName,
			String publicationName, String filePath)
		throws DLibraException, IOException, TransformerException
	{
		PublicationId publicationId = getPublicationId(
			getGroupId(groupPublicationName), publicationName);
		EditionId editionId = getEditionId(publicationId);

		Collection<Id> versionIds = publicationManager.getObjects(
			new EditionFilter(editionId),
			new OutputFilter(VersionId.class)).getResultIds();
		boolean recreateEmptyFolder = false;
		String emptyFolder = "";
		try {
			versionIds.remove(getVersionId(editionId, filePath));
			emptyFolder = filePath.substring(0, filePath.lastIndexOf("/") + 1);
			logger.debug("Will check for files in folder " + emptyFolder + ".");
			if (!emptyFolder.isEmpty()
					&& getFilePathsInFolder(groupPublicationName,
						publicationName, emptyFolder).size() == 1)
				recreateEmptyFolder = true;
			logger.debug("checked");
		}
		catch (IdNotFoundException ex) {
			logger.debug("File " + filePath + " not found, maybe a folder");
			// maybe it is a folder
			List<String> files = getFilePathsInFolder(groupPublicationName,
				publicationName, filePath);
			logger.debug("Will delete " + files.size() + " files");
			if (!files.isEmpty()) {
				for (String file : files) {
					if (file.startsWith("/"))
						file = file.substring(1);
					logger.debug("Deleting file " + file + " from folder "
							+ filePath);
					versionIds.remove(getVersionId(editionId, file));
				}
			}
			else {
				// it must be an empty folder
				try {
					filePath = EmptyFoldersUtility.convertReal2Dlibra(filePath);
					versionIds.remove(getVersionId(editionId, filePath));
					logger.debug("Deleting empty folder");
				}
				catch (IdNotFoundException ex2) {
					// if not, throw the original exception
					logger.debug("Nothing to delete, error");
					throw ex;
				}
			}
		}

		publicationManager.updateEditionVersions(editionId,
			(VersionId[]) versionIds.toArray(new VersionId[versionIds.size()]));

		if (recreateEmptyFolder) {
			createOrUpdateFile(versionUri, groupPublicationName,
				publicationName, emptyFolder, new ByteArrayInputStream(
						new byte[] {}), "text/plain");
		}

		try {
			regenerateManifest(groupPublicationName, publicationName,
				versionUri, getManifest(groupPublicationName, publicationName));
		}
		catch (JenaException e) {
			logger.warn("Manifest stored for publication "
					+ groupPublicationName + " is malformed");
		}
	}


	private DirectoryId getWorkspaceDir()
		throws RemoteException, DLibraException
	{
		User userData = userManager.getUserData(userLogin);
		return userData.getHomedir();
	}

}
