package pl.psnc.dl.wf4ever.connection;

import java.io.ByteArrayInputStream;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.DCTerms;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.EmptyFoldersUtility;
import pl.psnc.dl.wf4ever.RdfBuilder;
import pl.psnc.dlibra.common.Id;
import pl.psnc.dlibra.common.Info;
import pl.psnc.dlibra.common.InputFilter;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.File;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.FileId;
import pl.psnc.dlibra.metadata.FileInfo;
import pl.psnc.dlibra.metadata.FileManager;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.Version;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.metadata.VersionInfo;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.util.IOUtils;

public class FilesHelper
{

	private final static Logger logger = Logger.getLogger(FilesHelper.class);

	private DLibraDataSource dLibra;

	private PublicationManager publicationManager;

	private FileManager fileManager;

	private ContentServer contentServer;


	public FilesHelper(DLibraDataSource dLibraDataSource)
		throws RemoteException
	{
		this.dLibra = dLibraDataSource;

		this.publicationManager = dLibraDataSource.getMetadataServer()
				.getPublicationManager();
		this.fileManager = dLibraDataSource.getMetadataServer()
				.getFileManager();
		this.contentServer = dLibraDataSource.getContentServer();
	}


	/**
	 * Returns list of URIs of files in publication
	 */
	public List<String> getFilePathsInPublication(String groupPublicationName,
			String publicationName)
		throws RemoteException, DLibraException
	{
		return getFilePathsInFolder(groupPublicationName, publicationName, null);
	}


	/**
	* Returns filepaths of all files in a given folder, except for manifest.rdf.
	* @param groupPublicationName
	* @param publicationName
	* @param folder If null, all files in the publication will be returned
	* @return List of filepaths, starting with "/"
	* @throws RemoteException
	* @throws DLibraException
	*/
	public List<String> getFilePathsInFolder(String groupPublicationName,
			String publicationName, String folder)
		throws RemoteException, DLibraException
	{
		ArrayList<String> result = new ArrayList<String>();
		for (FileInfo fileInfo : getFilesInFolder(groupPublicationName,
			publicationName, folder).values()) {
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
	private Map<VersionId, FileInfo> getFilesInFolder(
			String groupPublicationName, String publicationName, String folder)
		throws RemoteException, DLibraException
	{
		PublicationId publicationId = dLibra.getPublicationsHelper()
				.getPublicationId(groupPublicationName, publicationName);
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
			if (EmptyFoldersUtility.isDlibraPath(filePath)
					&& EmptyFoldersUtility.convertDlibra2Real(filePath).equals(
						"/" + folder)) {
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


	/**
	* Returns input stream for a zipped content of file in a publication that are inside a given folder.
	* @param groupPublicationName
	* @param publicationName
	* @param folderNotStandardized
	* @return
	* @throws RemoteException
	* @throws DLibraException
	*/
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


	public InputStream getFileContents(String groupPublicationName,
			String publicationName, String filePath)
		throws IOException, DLibraException
	{
		PublicationId publicationId = dLibra.getPublicationsHelper()
				.getPublicationId(groupPublicationName, publicationName);
		EditionId editionId = getEditionId(publicationId);
		VersionId versionId = getVersionId(editionId, filePath);

		InputStream versionInputStream = contentServer
				.getVersionInputStream(versionId);
		return versionInputStream;
	}


	public String getFileMimeType(String groupPublicationName,
			String publicationName, String filePath)
		throws RemoteException, DLibraException
	{
		PublicationId publicationId = dLibra.getPublicationsHelper()
				.getPublicationId(groupPublicationName, publicationName);
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


	public String getFileMetadata(String groupPublicationName,
			String publicationName, String filePath, String fullPath)
		throws RemoteException, DLibraException, TransformerException
	{
		PublicationId publicationId = dLibra.getPublicationsHelper()
				.getPublicationId(groupPublicationName, publicationName);
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


	public void createOrUpdateFile(String versionUri,
			String groupPublicationName, String publicationName,
			String filePath, InputStream inputStream, String mimeType)
		throws IOException, DLibraException, TransformerException
	{
		createOrUpdateFile(versionUri, groupPublicationName, publicationName,
			filePath, inputStream, mimeType, true);
	}


	void createOrUpdateFile(String versionUri, String groupPublicationName,
			String publicationName, String filePath, InputStream inputStream,
			String mimeType, boolean generateManifest)
		throws IOException, DLibraException, TransformerException
	{
		PublicationId publicationId = dLibra.getPublicationsHelper()
				.getPublicationId(groupPublicationName, publicationName);
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
			byte[] buffer = new byte[DLibraDataSource.BUFFER_SIZE];
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
			VersionId[] copiesIds = copyVersions(publicationId, publicationId,
				null);
			VersionId[] versionIds = Arrays.copyOf(copiesIds,
				copiesIds.length + 1);
			versionIds[versionIds.length - 1] = createdVersion.getId();
			publicationManager.updateEditionVersions(editionId, versionIds);
		}

		if (generateManifest) {
			try {
				dLibra.getManifestHelper().regenerateManifest(
					groupPublicationName,
					publicationName,
					versionUri,
					dLibra.getManifestHelper().getManifest(
						groupPublicationName, publicationName));
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


	public void deleteFile(String versionUri, String groupPublicationName,
			String publicationName, String filePath)
		throws DLibraException, IOException, TransformerException
	{
		PublicationId publicationId = dLibra.getPublicationsHelper()
				.getPublicationId(groupPublicationName, publicationName);
		EditionId editionId = getEditionId(publicationId);

		boolean recreateEmptyFolder = false;
		String emptyFolder = "";
		HashSet<VersionId> exclude = new HashSet<VersionId>();
		try {
			exclude.add(getVersionId(editionId, filePath));
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
					exclude.add(getVersionId(editionId, file));
				}
			}
			else {
				// it must be an empty folder
				try {
					filePath = EmptyFoldersUtility.convertReal2Dlibra(filePath);
					exclude.add(getVersionId(editionId, filePath));
					logger.debug("Deleting empty folder");
				}
				catch (IdNotFoundException ex2) {
					// if not, throw the original exception
					logger.debug("Nothing to delete, error");
					throw ex;
				}
			}
		}

		VersionId[] copiesIds = copyVersions(publicationId, publicationId,
			exclude);
		publicationManager.updateEditionVersions(editionId, copiesIds);

		if (recreateEmptyFolder) {
			createOrUpdateFile(versionUri, groupPublicationName,
				publicationName, emptyFolder, new ByteArrayInputStream(
						new byte[] {}), "text/plain");
		}

		try {
			dLibra.getManifestHelper().regenerateManifest(
				groupPublicationName,
				publicationName,
				versionUri,
				dLibra.getManifestHelper().getManifest(groupPublicationName,
					publicationName));
		}
		catch (JenaException e) {
			logger.warn("Manifest stored for publication "
					+ groupPublicationName + " is malformed");
		}
	}


	public EditionId getEditionId(PublicationId publicationId)
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


	private VersionId getVersionId(EditionId editionId, String filePath)
		throws IdNotFoundException, RemoteException, DLibraException
	{
		VersionId versionId = (VersionId) fileManager.getObjects(
			new FileFilter().setEditionId(editionId)
					.setFileName("/" + filePath),
			new OutputFilter(VersionId.class)).getResultId();
		return versionId;
	}


	// TODO this is ridiculous, dLibra API should be changed
	VersionId[] copyVersions(PublicationId sourcePublicationId,
			PublicationId targetPublicationId, Set<VersionId> exclude)
		throws IOException, DLibraException
	{
		EditionId sourceEditionId = dLibra.getFilesHelper().getEditionId(
			sourcePublicationId);
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
			if (exclude != null && exclude.contains(id)) {
				continue;
			}
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
			byte[] buffer = new byte[DLibraDataSource.BUFFER_SIZE];
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
}
