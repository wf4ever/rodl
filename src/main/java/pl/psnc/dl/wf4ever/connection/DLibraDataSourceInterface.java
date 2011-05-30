package pl.psnc.dl.wf4ever.connection;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.List;

import javax.xml.transform.TransformerException;

import pl.psnc.dlibra.metadata.GroupPublicationInfo;
import pl.psnc.dlibra.metadata.PublicationInfo;
import pl.psnc.dlibra.service.DLibraException;

import com.hp.hpl.jena.shared.JenaException;

public interface DLibraDataSourceInterface
{

	/**
	 * Creates user in dLibra, equivalent to workspace in ROSRS.
	 * @param login
	 * @param password
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public abstract void createUser(String login, String password)
		throws RemoteException, DLibraException;


	/**
	 * Deletes user from dLibra, equivalent to workspace in ROSRS. 
	 * @param login
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public abstract void deleteUser(String login)
		throws RemoteException, DLibraException;


	/**
	 * Returns list of all group publications (ROs) of the current user.	
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public abstract List<GroupPublicationInfo> listUserGroupPublications()
		throws RemoteException, DLibraException;


	/**
	 * Creates a new group publication (RO) for the current user.
	 * @param groupPublicationName
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public abstract void createGroupPublication(String groupPublicationName)
		throws RemoteException, DLibraException;


	/**
	 * Deletes a group publication (RO) for the current user.
	 * @param groupPublicationName
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public abstract void deleteGroupPublication(String groupPublicationName)
		throws RemoteException, DLibraException;


	/**
	 * Returns list of all publications (versions) for given group publication (RO).
	 * @param groupPublicationName
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public abstract List<PublicationInfo> listPublicationsInGroup(
			String groupPublicationName)
		throws RemoteException, DLibraException;


	/**
	 * Creates new publication (version) in a group publication (RO). If basePublicationName is not
	 * null, then the new publication is a copy of base publication. This method creates a manifest.rdf 
	 * with empty RO metadata.
	 * @param groupPublicationName
	 * @param publicationName
	 * @param basePublicationName Optional name of base publication to copy from
	 * @param versionUri URI of this RO version, used for creating manifest
	 * @throws DLibraException
	 * @throws IOException
	 * @throws TransformerException
	 */
	public abstract void createPublication(String groupPublicationName,
			String publicationName, String basePublicationName,
			String versionUri)
		throws DLibraException, IOException, TransformerException;


	/**
	 * Deletes publication (version) from a group publication (RO).
	 * @param groupPublicationName
	 * @param publicationName
	 * @param versionUri URI of this RO version, used for modifying manifest
	 * @throws DLibraException
	 * @throws IOException
	 * @throws TransformerException
	 */
	public abstract void deletePublication(String groupPublicationName,
			String publicationName, String versionUri)
		throws DLibraException, IOException, TransformerException;


	/**
	 * Returns filepaths of all files in a given folder, except for manifest.rdf.
	 * @param groupPublicationName
	 * @param publicationName
	 * @param folder If null, all files in the publication will be returned
	 * @return List of filepaths, starting with "/"
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public abstract List<String> getFilePathsInFolder(
			String groupPublicationName, String publicationName, String folder)
		throws RemoteException, DLibraException;


	/**
	 * Returns input stream for a zipped content of a publication.
	 * @param groupPublicationName
	 * @param publicationName
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public abstract InputStream getZippedPublication(
			String groupPublicationName, String publicationName)
		throws RemoteException, DLibraException;


	/**
	 * Returns input stream for a zipped content of file in a publication that are inside a given folder.
	 * @param groupPublicationName
	 * @param publicationName
	 * @param folderNotStandardized Folder name with or without the "/" at the end
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public abstract InputStream getZippedFolder(String groupPublicationName,
			String publicationName, String folderNotStandardized)
		throws RemoteException, DLibraException;


	/**
	 * Returns manifest.rdf serialized as String.
	 * @param groupPublicationName
	 * @param publicationName
	 * @return manifest.rdf serialized as String
	 * @throws IOException
	 * @throws DLibraException
	 */
	public abstract String getManifest(String groupPublicationName,
			String publicationName)
		throws IOException, DLibraException;


	/**
	 * Updated manifest content. Only RO metadata is updated, any changes to the resource 
	 * list by users will be ignored. 
	 * @param versionUri
	 * @param groupPublicationName
	 * @param publicationName
	 * @param manifest Manifest content as serialized String
	 * @throws DLibraException
	 * @throws IOException
	 * @throws TransformerException
	 * @throws JenaException
	 */
	public abstract void updateManifest(String versionUri,
			String groupPublicationName, String publicationName, String manifest)
		throws DLibraException, IOException, TransformerException,
		JenaException;


	// maybe we should merge getFileContents and getFileMimeType in one method?
	public abstract InputStream getFileContents(String groupPublicationName,
			String publicationName, String filePath)
		throws IOException, DLibraException;


	public abstract String getFileMimeType(String groupPublicationName,
			String publicationName, String filePath)
		throws RemoteException, DLibraException;


	public abstract String getFileMetadata(String groupPublicationName,
			String publicationName, String filePath, String fullPath)
		throws RemoteException, DLibraException, TransformerException;


	public abstract void createOrUpdateFile(String versionUri,
			String groupPublicationName, String publicationName,
			String filePath, InputStream inputStream, String mimeType)
		throws IOException, DLibraException, TransformerException;


	public abstract void deleteFile(String versionUri,
			String groupPublicationName, String publicationName, String filePath)
		throws DLibraException, IOException, TransformerException;

}