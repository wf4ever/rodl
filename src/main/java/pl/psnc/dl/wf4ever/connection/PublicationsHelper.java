/**
 * 
 */
package pl.psnc.dl.wf4ever.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.RdfBuilder;
import pl.psnc.dlibra.common.Info;
import pl.psnc.dlibra.common.InputFilter;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.DirectoryFilter;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.DirectoryManager;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.ElementInfo;
import pl.psnc.dlibra.metadata.File;
import pl.psnc.dlibra.metadata.FileManager;
import pl.psnc.dlibra.metadata.GroupPublicationInfo;
import pl.psnc.dlibra.metadata.LibCollectionFilter;
import pl.psnc.dlibra.metadata.LibCollectionId;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationInfo;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.Version;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.metadata.attributes.AttributeInfo;
import pl.psnc.dlibra.search.AbstractSearchResult;
import pl.psnc.dlibra.search.QueryParseException;
import pl.psnc.dlibra.search.local.AdvancedQuery;
import pl.psnc.dlibra.search.local.QueryElement;
import pl.psnc.dlibra.search.server.SearchServer;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.DuplicatedValueException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.user.User;
import pl.psnc.dlibra.user.UserManager;

import com.hp.hpl.jena.shared.JenaException;

/**
 * @author piotrhol
 *
 */
public class PublicationsHelper
{

	private final static Logger logger = Logger
			.getLogger(PublicationsHelper.class);

	private DLibraDataSource dLibra;

	private PublicationManager publicationManager;

	private DirectoryManager directoryManager;

	private UserManager userManager;

	private FileManager fileManager;

	private ContentServer contentServer;


	public PublicationsHelper(DLibraDataSource dLibraDataSource)
		throws RemoteException
	{
		this.dLibra = dLibraDataSource;

		publicationManager = dLibraDataSource.getMetadataServer()
				.getPublicationManager();
		directoryManager = dLibraDataSource.getMetadataServer()
				.getDirectoryManager();
		userManager = dLibraDataSource.getUserManager();
		fileManager = dLibraDataSource.getMetadataServer().getFileManager();
		contentServer = dLibraDataSource.getContentServer();
	}


	/**
	* Returns list of all group publications (ROs) of the current user.
	* @return
	* @throws RemoteException
	* @throws DLibraException
	*/
	public List<AbstractPublicationInfo> listUserGroupPublications()
		throws RemoteException, DLibraException
	{
		DirectoryId workspaceDir = getWorkspaceDirectoryId();
		Collection<Info> resultInfos = directoryManager
				.getObjects(
					new DirectoryFilter(null, workspaceDir)
							.setGroupStatus(Publication.PUB_GROUP_ROOT)
							.setState(
								(byte) (Publication.PUB_STATE_ALL - Publication.PUB_STATE_PERMANENT_DELETED)),
					new OutputFilter(ElementInfo.class, List.class))
				.getResultInfos();

		ArrayList<AbstractPublicationInfo> result = new ArrayList<AbstractPublicationInfo>();
		for (Info info : resultInfos) {
			if (info instanceof GroupPublicationInfo) {
				result.add((GroupPublicationInfo) info);
			}
		}
		return result;
	}


	/**
	 * Performs search using attributes.
	 * @param queryParameters List of pairs <attribute_RDF_name, value>. Unrecognized RDF names are ignored.
	 * @return List of publication infos.
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public List<AbstractPublicationInfo> listUserGroupPublications(
			MultivaluedMap<String, String> queryParameters)
		throws RemoteException, DLibraException
	{
		SearchServer searchServer = dLibra.getSearchServer();
		if (searchServer == null) {
			logger.error("Search server is null, returning list of group publications");
			return listUserGroupPublications();
		}

		AdvancedQuery query = null;

		for (Map.Entry<String, List<String>> entry : queryParameters.entrySet()) {
			String attributeRdfName = entry.getKey();
			AttributeInfo info = null;
			try {
				info = dLibra.getAttributesHelper().getAttributeInfo(
					attributeRdfName);
			}
			catch (IdNotFoundException e) {
				logger.error("Id null not found, should not happen", e);
			}
			if (info == null) {
				logger.debug(String.format(
					"Query param %s is not a valid dLibra attribute",
					attributeRdfName));
				continue;
			}
			if (query == null)
				query = new AdvancedQuery();
			logger.debug(String.format("Adding query element %s=%s",
				info.getId(), entry.getValue().get(0)));
			query.addQueryElement(new QueryElement(info.getId(), entry
					.getValue().get(0)));
		}

		if (query == null) {
			logger.debug("No dLibra query params, returning list of group publications");
			return listUserGroupPublications();
		}

		query.setSearchRemoteResources(false);
		query.setExpandable(false);
		query.setAggregate(false);

		query.setLibCollectionId((LibCollectionId) dLibra
				.getMetadataServer()
				.getLibCollectionManager()
				.getObjects(new LibCollectionFilter(),
					new OutputFilter(LibCollectionId.class)).getResultId());
		query.setLanguageName(AttributesHelper.ATTRIBUTE_LANGUAGE);

		List<AbstractPublicationInfo> result = new ArrayList<AbstractPublicationInfo>();
		try {
			List<AbstractSearchResult> hits = searchServer.getSearchManager()
					.getSearchResults(
						searchServer.getSearchManager().search(query));
			for (AbstractSearchResult hit : hits) {
				Publication publication = hit.getRoot();
				result.add((AbstractPublicationInfo) publication.getInfo());
			}
		}
		catch (QueryParseException e) {
			logger.error(
				String.format("Error when parsing query %s.", query.toString()),
				e);
			return listUserGroupPublications();
		}
		return result;
	}


	/**
	* Creates a new group publication (RO) for the current user.
	* @param groupPublicationName
	* @throws RemoteException
	* @throws DLibraException
	*/
	public void createGroupPublication(String groupPublicationName)
		throws RemoteException, DLibraException
	{
		DirectoryId parent = getWorkspaceDirectoryId();
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


	/**
	* Deletes a group publication (RO) for the current user.
	* @param groupPublicationName
	* @throws RemoteException
	* @throws DLibraException
	*/
	public void deleteGroupPublication(String groupPublicationName)
		throws RemoteException, DLibraException
	{
		PublicationId groupId = getGroupId(groupPublicationName);
		publicationManager.removePublication(groupId, true,
			"Research object removed");
	}


	/**
	* Returns list of all publications (versions) for given group publication (RO).
	* @param groupPublicationName
	* @return
	* @throws RemoteException
	* @throws DLibraException
	*/
	public List<PublicationInfo> listPublicationsInGroup(
			String groupPublicationName)
		throws RemoteException, DLibraException
	{
		PublicationId groupId = getGroupId(groupPublicationName);

		InputFilter in = new PublicationFilter(null, groupId)
				.setGroupStatus(Publication.PUB_GROUP_LEAF)
				.setPublicationState(
					(byte) (Publication.PUB_STATE_ALL - Publication.PUB_STATE_PERMANENT_DELETED));
		OutputFilter out = new OutputFilter(AbstractPublicationInfo.class,
				List.class);
		Collection<Info> resultInfos = publicationManager.getObjects(in, out)
				.getResultInfos();

		ArrayList<PublicationInfo> result = new ArrayList<PublicationInfo>();
		for (Info info : resultInfos) {
			result.add((PublicationInfo) info);
		}
		return result;
	}


	/**
	* Creates new publication (version) in a group publication (RO).
	* <p>
	* If basePublicationName is not null, then the new publication is a copy of
	* base publication.
	* @param groupPublicationName
	* @param publicationName
	* @param basePublicationName Optional name of base publication to copy from
	* @param versionURI URI of the new RO version, used for creating manifest
	* @throws DLibraException
	* @throws IOException
	* @throws TransformerException
	*/
	public void createPublication(String groupPublicationName,
			String publicationName, String basePublicationName,
			String versionURI)
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

		Publication publication = getNewPublication(publicationName, groupId);
		PublicationId publicationId = publicationManager
				.createPublication(publication);
		if (basePublicationName != null && !basePublicationName.isEmpty()) {
			PublicationId basePublicationId = getPublicationId(groupId,
				basePublicationName);
			preparePublicationAsACopy(groupPublicationName, publicationName,
				versionURI, publicationId, basePublicationName,
				basePublicationId);
		}
		else {
			preparePublicationAsNew(groupPublicationName, publicationName,
				versionURI, publicationId);
		}

		addHasVersionPropertyToAll(groupPublicationName, versionURI);

		//		publishPublication(publicationId);
	}


	@SuppressWarnings("unused")
	private void publishPublication(PublicationId publicationId)
		throws RemoteException, DLibraException
	{
		Edition edition = dLibra.getEditionHelper().getEdition(publicationId);
		edition.setPublished(true);
		publicationManager.setEditionData(edition);

		dLibra.getMetadataServer()
				.getLibCollectionManager()
				.addToCollections(Arrays.asList(dLibra.getCollectionId()),
					Arrays.asList((ElementId) publicationId), false);
	}


	private Publication getNewPublication(String publicationName,
			PublicationId groupId)
		throws RemoteException, DLibraException
	{
		Publication publication = new Publication(null,
				getWorkspaceDirectoryId());
		publication.setParentPublicationId(groupId);
		publication.setName(publicationName);
		publication.setPosition(0);
		publication.setGroupStatus(Publication.PUB_GROUP_LEAF);
		publication.setSecured(false);
		publication.setState(Publication.PUB_STATE_ACTUAL);
		return publication;
	}


	private EditionId preparePublicationAsNew(String groupPublicationName,
			String publicationName, String versionUri,
			PublicationId publicationId)
		throws DLibraException, AccessDeniedException, IdNotFoundException,
		RemoteException, TransformerException, IOException
	{
		Date creationDate = new Date();

		File file = new File("application/rdf+xml", publicationId, "/"
				+ Constants.MANIFEST_FILENAME);

		Version createdVersion = fileManager.createVersion(file, 0,
			creationDate, "");

		String manifest = dLibra.getManifestHelper().createInitialManifest(
			versionUri, groupPublicationName, "", "", "", publicationName,
			RdfBuilder.createDateLiteral(creationDate));

		OutputStream output = contentServer
				.getVersionOutputStream(createdVersion.getId());
		output.write(manifest.getBytes());
		output.close();

		publicationManager.setMainFile(publicationId,
			createdVersion.getFileId());

		EditionId editionId = dLibra.getEditionHelper().createEdition(
			publicationName, publicationId,
			new VersionId[] { createdVersion.getId()});

		dLibra.getAttributesHelper().updateMetadataAttributes(
			groupPublicationName, publicationName, manifest);
		dLibra.getAttributesHelper().updateCreatedAttribute(
			groupPublicationName, publicationName, creationDate.toString());

		return editionId;
	}


	private void preparePublicationAsACopy(String groupPublicationName,
			String publicationName, String versionURI,
			PublicationId publicationId, String basePublicationName,
			PublicationId basePublicationId)
		throws RemoteException, DLibraException, AccessDeniedException,
		IdNotFoundException, IOException, TransformerException
	{
		VersionId[] copyVersions = dLibra.getFilesHelper().copyVersions(
			basePublicationId, publicationId);
		Edition edition = new Edition(null, publicationId, false);
		edition.setName(publicationName);
		publicationManager.createEdition(edition, copyVersions);

		try {
			String baseVersionURI = versionURI.substring(0,
				versionURI.lastIndexOf("/") + 1)
					+ basePublicationName;
			dLibra.getManifestHelper().regenerateManifest(
				groupPublicationName,
				publicationName,
				versionURI,
				dLibra.getManifestHelper().getManifest(groupPublicationName,
					basePublicationName), baseVersionURI);
			dLibra.getAttributesHelper().updateMetadataAttributes(
				groupPublicationName,
				publicationName,
				dLibra.getManifestHelper().getManifest(groupPublicationName,
					basePublicationName));
		}
		catch (JenaException e) {
			logger.warn("Manifest stored for publication "
					+ groupPublicationName + " is malformed");
		}
		catch (IncorrectManifestException e) {
			logger.warn(String.format(
				"Manifest stored for publication %s/%s is incorrect (%s)",
				groupPublicationName, basePublicationName, e.getMessage()));
		}
	}


	private void addHasVersionPropertyToAll(String groupPublicationName,
			String versionURI)
		throws RemoteException, DLibraException, IOException,
		TransformerException
	{
		List<PublicationInfo> list = listPublicationsInGroup(groupPublicationName);
		for (PublicationInfo p : list) {
			try {
				String pubVersionURI = versionURI.substring(0,
					versionURI.lastIndexOf("/") + 1)
						+ p.getLabel();
				logger.debug(String
						.format(
							"Will regenerate manifest and add hasVersion for version %s",
							p.getLabel()));
				dLibra.getManifestHelper().regenerateManifest(
					groupPublicationName,
					p.getLabel(),
					pubVersionURI,
					dLibra.getManifestHelper().getManifest(
						groupPublicationName, p.getLabel()));
			}
			catch (JenaException e) {
				logger.warn("Manifest stored for publication "
						+ groupPublicationName + " is malformed");
			}
			catch (IncorrectManifestException e) {
				logger.warn(String.format(
					"Manifest stored for publication %s/%s is incorrect (%s)",
					groupPublicationName, p.getLabel(), e.getMessage()));
				logger.warn(dLibra.getManifestHelper().getManifest(
					groupPublicationName, p.getLabel()));
			}
		}
	}


	/**
	* Deletes publication (version) from a group publication (RO).
	* @param groupPublicationName
	* @param publicationName
	* @param versionUri URI of this RO version, used for modifying manifest
	* @throws DLibraException
	* @throws IOException
	* @throws TransformerException
	*/
	public void deletePublication(String groupPublicationName,
			String publicationName, String versionUri)
		throws DLibraException, IOException, TransformerException
	{
		PublicationId publicationId = getPublicationId(
			getGroupId(groupPublicationName), publicationName);

		publicationManager.removePublication(publicationId, true,
			"Research Object Version removed.");

		addHasVersionPropertyToAll(groupPublicationName, versionUri);
	}


	PublicationId getGroupId(String groupPublicationName)
		throws RemoteException, DLibraException
	{
		Collection<Info> resultInfos = directoryManager
				.getObjects(
					new DirectoryFilter(null, getWorkspaceDirectoryId())
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


	PublicationId getPublicationId(PublicationId groupId, String publicationName)
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


	PublicationId getPublicationId(String groupPublicationName,
			String publicationName)
		throws RemoteException, DLibraException
	{
		return getPublicationId(getGroupId(groupPublicationName),
			publicationName);

	}


	private DirectoryId getWorkspaceDirectoryId()
		throws RemoteException, DLibraException
	{
		User userData = userManager.getUserData(dLibra.getUserLogin());
		return userData.getHomedir();
	}


	/**
	* Returns input stream for a zipped content of a publication.
	* @param groupPublicationName
	* @param publicationName
	* @return
	* @throws RemoteException
	* @throws DLibraException
	*/
	public InputStream getZippedPublication(String groupPublicationName,
			String publicationName)
		throws RemoteException, DLibraException
	{
		return dLibra.getFilesHelper().getZippedFolder(groupPublicationName,
			publicationName, null);
	}

}
