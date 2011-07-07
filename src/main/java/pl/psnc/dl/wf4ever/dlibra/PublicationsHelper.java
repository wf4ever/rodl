/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import com.ibm.icu.text.SimpleDateFormat;

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
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationInfo;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.Version;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.metadata.attributes.AttributeInfo;
import pl.psnc.dlibra.search.AbstractSearchHit;
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

/**
 * @author piotrhol
 * 
 */
public class PublicationsHelper {

	private final static Logger logger = Logger
			.getLogger(PublicationsHelper.class);

	private DLibraDataSource dLibra;

	private PublicationManager publicationManager;

	private DirectoryManager directoryManager;

	private UserManager userManager;

	private FileManager fileManager;

	private ContentServer contentServer;

	private static final String QP_PUBLISHED_FROM = "PublishedFrom";

	private static final String QP_PUBLISHED_UNTIL = "PublishedUntil";

	private static final String[] NON_RDF_QUERY_PARAMS = { QP_PUBLISHED_FROM,
			QP_PUBLISHED_UNTIL };

	public PublicationsHelper(DLibraDataSource dLibraDataSource)
			throws RemoteException {
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
	 * 
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public List<AbstractPublicationInfo> listUserGroupPublications()
			throws RemoteException, DLibraException {
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
	 * 
	 * @param queryParameters
	 *            List of pairs <attribute_RDF_name, value>. Unrecognized RDF
	 *            names are ignored. Additionally, 'PublishedFrom' and
	 *            'PublishedUntil' are also parsed as referring to published
	 *            editions creation dates.
	 * @return List of publication infos.
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public List<AbstractPublicationInfo> listUserPublications(
			MultivaluedMap<String, String> queryParameters)
			throws RemoteException, DLibraException {
		SearchServer searchServer = dLibra.getSearchServer();
		if (searchServer == null) {
			logger.error("Search server is null, returning list of group publications");
			return listUserGroupPublications();
		}

		AdvancedQuery query = new AdvancedQuery();

		for (Map.Entry<String, List<String>> entry : queryParameters.entrySet()) {
			String attributeRdfName = entry.getKey();
			String firstValue = entry.getValue().isEmpty() ? null : entry
					.getValue().get(0);
			if (Arrays.asList(NON_RDF_QUERY_PARAMS).contains(attributeRdfName)) {
				if (attributeRdfName.equals(QP_PUBLISHED_FROM)) {
					setQueryDate(query, firstValue, false);
				} else if (attributeRdfName.equals(QP_PUBLISHED_UNTIL)) {
					setQueryDate(query, firstValue, true);
				}
			} else {
				AttributeInfo info = null;
				info = dLibra.getAttributesHelper().getAttributeInfo(
						attributeRdfName);
				if (info == null) {
					logger.debug(String.format(
							"Query param %s is not a valid dLibra attribute",
							attributeRdfName));
					continue;
				}
				logger.debug(String.format("Adding query element %s=%s",
						info.getId(), entry.getValue().get(0)));
				query.addQueryElement(new QueryElement(info.getId(), entry
						.getValue().get(0)));
			}
		}

		query.setSearchRemoteResources(false);
		query.setExpandable(false);
		query.setAggregate(false);

		query.setLibCollectionId(dLibra.getCollectionId());
		query.setLanguageName(AttributesHelper.ATTRIBUTE_LANGUAGE);

		List<AbstractPublicationInfo> result = new ArrayList<AbstractPublicationInfo>();
		try {
			List<AbstractSearchHit> hits = searchServer.getSearchManager()
					.search(query);
			logger.debug("Found " + hits.size() + " search results");
			for (AbstractSearchHit hit : hits) {
				try {
					EditionId editionId = (EditionId) hit.getElementId();
					Edition edition = dLibra.getEditionHelper().getEdition(
							editionId);
					PublicationId publicationId = (PublicationId) edition
							.getParentId();
					Publication publication = getPublication(publicationId);
					logger.debug("Found search result: "
							+ publication.getName());
					result.add((AbstractPublicationInfo) publication.getInfo());
				} catch (IdNotFoundException e) {
					logger.error("Could not find publication: "
							+ e.getLocalizedMessage());
				}
			}
		} catch (QueryParseException e) {
			logger.error(
					String.format("Error when parsing query %s.",
							query.toString()), e);
			return listUserGroupPublications();
		}
		return result;
	}

	private void setQueryDate(AdvancedQuery query, String firstValue,
			boolean until) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date date = sdf.parse(firstValue);
			if (date == null) {
				logger.error("Could not parse date " + firstValue);
			} else {
				if (until)
					query.setDateUntil(date);
				else
					query.setDate(date);
			}
		} catch (ParseException e) {
			logger.error(String.format("Could not parse date %s (%s)",
					firstValue, e.getMessage()));
		}
	}

	private Publication getPublication(PublicationId publicationId)
			throws IdNotFoundException, RemoteException, DLibraException {
		InputFilter in = new PublicationFilter(publicationId);
		OutputFilter out = new OutputFilter(Publication.class);
		return (Publication) publicationManager.getObjects(in, out).getResult();
	}

	/**
	 * Creates a new group publication (RO) for the current user.
	 * 
	 * @param groupPublicationName
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public void createGroupPublication(String groupPublicationName)
			throws RemoteException, DLibraException {
		DirectoryId parent = getWorkspaceDirectoryId();
		try {
			getGroupId(groupPublicationName);
			throw new DuplicatedValueException(null, "RO already exists",
					groupPublicationName);
		} catch (IdNotFoundException e) {
			// OK - group does not exist
		}

		Publication publication = new Publication(parent);
		publication.setGroupStatus(Publication.PUB_GROUP_ROOT);
		publication.setName(groupPublicationName);

		publicationManager.createPublication(publication);
	}

	/**
	 * Deletes a group publication (RO) for the current user.
	 * 
	 * @param groupPublicationName
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public void deleteGroupPublication(String groupPublicationName)
			throws RemoteException, DLibraException {
		PublicationId groupId = getGroupId(groupPublicationName);
		publicationManager.removePublication(groupId, true,
				"Research object removed");
	}

	/**
	 * Returns list of all publications (versions) for given group publication
	 * (RO).
	 * 
	 * @param groupPublicationName
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public List<PublicationInfo> listPublicationsInGroup(
			String groupPublicationName) throws RemoteException,
			DLibraException {
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
	 * 
	 * @param groupPublicationName
	 * @param publicationName
	 * @param basePublicationName
	 *            Optional name of base publication to copy from
	 * @param versionURI
	 *            URI of the new RO version, used for creating manifest
	 * @throws DLibraException
	 * @throws IOException
	 * @throws TransformerException
	 */
	public void createPublication(String groupPublicationName,
			String publicationName, String basePublicationName,
			String versionURI) throws DLibraException, IOException,
			TransformerException {
		PublicationId groupId = getGroupId(groupPublicationName);
		try {
			getPublicationId(groupId, publicationName);
			throw new DuplicatedValueException(null,
					"RO Version already exists", groupPublicationName + "/"
							+ publicationName);
		} catch (IdNotFoundException e) {
			// OK - the publication does not exist
		}

		Publication publication = getNewPublication(publicationName, groupId);
		PublicationId publicationId = publicationManager
				.createPublication(publication);
		logger.debug(String.format("Created publication %s with id %s",
				publication.getName(), publicationId));
		if (basePublicationName != null && !basePublicationName.isEmpty()) {
			PublicationId basePublicationId = getPublicationId(groupId,
					basePublicationName);
			preparePublicationAsACopy(groupPublicationName, publicationName,
					versionURI, publicationId, basePublicationName,
					basePublicationId);
		} else {
			preparePublicationAsNew(groupPublicationName, publicationName,
					versionURI, publicationId);
		}

		addHasVersionPropertyToAll(groupPublicationName, versionURI);

		dLibra.getMetadataServer()
				.getLibCollectionManager()
				.addToCollections(Arrays.asList(dLibra.getCollectionId()),
						Arrays.asList((ElementId) publicationId), false);
	}

	public void publishPublication(String groupPublicationName,
			String publicationName) throws RemoteException, DLibraException {
		unpublishPublication(groupPublicationName, publicationName);

		Edition edition = dLibra.getEditionHelper().getLastEdition(
				groupPublicationName, publicationName);
		edition.setPublished(true);
		publicationManager.setEditionData(edition);
	}

	public void unpublishPublication(String groupPublicationName,
			String publicationName) throws RemoteException, DLibraException {
		Set<Edition> editions = dLibra.getEditionHelper().getEditionList(
				groupPublicationName, publicationName);
		for (Edition edition : editions) {
			if (edition.isPublished()) {
				edition.setPublished(false);
				publicationManager.setEditionData(edition);
			}
		}
	}

	private Publication getNewPublication(String publicationName,
			PublicationId groupId) throws RemoteException, DLibraException {
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
			PublicationId publicationId) throws DLibraException,
			AccessDeniedException, IdNotFoundException, RemoteException,
			TransformerException, IOException {
		Date creationDate = new Date();

		File file = new File("application/rdf+xml", publicationId, "/"
				+ Constants.MANIFEST_FILENAME);

		Version createdVersion = fileManager.createVersion(file, 0,
				creationDate, "");

		InputStream manifest = dLibra.getManifestHelper()
				.createInitialManifest(versionUri, groupPublicationName, "",
						"", "", publicationName,
						RdfBuilder.createDateLiteral(creationDate));

		OutputStream output = contentServer
				.getVersionOutputStream(createdVersion.getId());
		try {
			byte[] buffer = new byte[DLibraDataSource.BUFFER_SIZE];
			int bytesRead = 0;
			while ((bytesRead = manifest.read(buffer)) > 0) {
				output.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			logger.error("Error when saving manifest to content server", e);
		} finally {
			output.close();
			manifest.reset();
		}

		publicationManager.setMainFile(publicationId,
				createdVersion.getFileId());

		EditionId editionId = dLibra.getEditionHelper().createEdition(
				publicationName, publicationId, new VersionId[] {});

		publicationManager.addEditionVersion(editionId, createdVersion.getId());

		dLibra.getAttributesHelper().updateMetadataAttributes(
				groupPublicationName, publicationName, manifest);
		dLibra.getAttributesHelper().updateCreatedAttribute(
				groupPublicationName, publicationName, creationDate.toString());

		return editionId;
	}

	private void preparePublicationAsACopy(String groupPublicationName,
			String publicationName, String versionURI,
			PublicationId publicationId, String basePublicationName,
			PublicationId basePublicationId) throws RemoteException,
			DLibraException, AccessDeniedException, IdNotFoundException,
			IOException, TransformerException {
		VersionId[] copyVersions = dLibra.getFilesHelper().copyVersions(
				basePublicationId, publicationId);
		Edition edition = new Edition(null, publicationId, false);
		edition.setName(publicationName);
		publicationManager.createEdition(edition, copyVersions);

		EditionId baseEditionId = dLibra.getEditionHelper().getLastEditionId(
				groupPublicationName, basePublicationName);

		String baseVersionURI = versionURI.substring(0,
				versionURI.lastIndexOf("/") + 1)
				+ basePublicationName;
		dLibra.getManifestHelper().regerenerateManifestSafe(versionURI,
				groupPublicationName, publicationName, baseVersionURI);
		dLibra.getAttributesHelper().updateMetadataAttributes(
				groupPublicationName, publicationName,
				dLibra.getManifestHelper().getManifest(baseEditionId));
	}

	private void addHasVersionPropertyToAll(String groupPublicationName,
			String versionURI) throws RemoteException, DLibraException,
			IOException, TransformerException {
		List<PublicationInfo> list = listPublicationsInGroup(groupPublicationName);
		for (PublicationInfo p : list) {
			String pubVersionURI = versionURI.substring(0,
					versionURI.lastIndexOf("/") + 1)
					+ p.getLabel();
			logger.debug(String
					.format("Will regenerate manifest and add hasVersion for version %s",
							p.getLabel()));
			dLibra.getManifestHelper().regerenerateManifestSafe(pubVersionURI,
					groupPublicationName, p.getLabel());
		}
	}

	/**
	 * Deletes publication (version) from a group publication (RO).
	 * 
	 * @param groupPublicationName
	 * @param publicationName
	 * @param versionUri
	 *            URI of this RO version, used for modifying manifest
	 * @throws DLibraException
	 * @throws IOException
	 * @throws TransformerException
	 */
	public void deletePublication(String groupPublicationName,
			String publicationName, String versionUri) throws DLibraException,
			IOException, TransformerException {
		PublicationId publicationId = getPublicationId(
				getGroupId(groupPublicationName), publicationName);

		publicationManager.removePublication(publicationId, true,
				"Research Object Version removed.");

		addHasVersionPropertyToAll(groupPublicationName, versionUri);
	}

	PublicationId getGroupId(String groupPublicationName)
			throws RemoteException, DLibraException {
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
			throws RemoteException, DLibraException {
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

	public PublicationId getPublicationId(String groupPublicationName,
			String publicationName) throws RemoteException, DLibraException {
		return getPublicationId(getGroupId(groupPublicationName),
				publicationName);

	}

	private DirectoryId getWorkspaceDirectoryId() throws RemoteException,
			DLibraException {
		User userData = userManager.getUserData(dLibra.getUserLogin());
		return userData.getHomedir();
	}

	/**
	 * Returns input stream for a zipped content of a publication.
	 * 
	 * @param groupPublicationName
	 * @param publicationName
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public InputStream getZippedPublication(String groupPublicationName,
			String publicationName) throws RemoteException, DLibraException {
		return dLibra.getFilesHelper().getZippedFolder(
				dLibra.getEditionHelper().getLastEditionId(
						groupPublicationName, publicationName), null);
	}

	public InputStream getZippedPublication(EditionId editionId)
			throws RemoteException, DLibraException {
		return dLibra.getFilesHelper().getZippedFolder(editionId, null);
	}

}
