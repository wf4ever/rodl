/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import pl.psnc.dlibra.common.DLObject;
import pl.psnc.dlibra.common.Id;
import pl.psnc.dlibra.common.InputFilter;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;

/**
 * @author piotrhol
 *
 */
public class EditionHelper
{

	private DLibraDataSource dLibra;

	private PublicationManager publicationManager;


	public EditionHelper(DLibraDataSource dLibraDataSource)
		throws RemoteException
	{
		this.dLibra = dLibraDataSource;
		publicationManager = dLibraDataSource.getMetadataServer()
				.getPublicationManager();
	}


	/**
	 * Returns the most recently created edition of the RO version (publication).
	 * @param publicationId Id of the publication (RO version).
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException in case no edition is found
	 */
	public Edition getLastEdition(PublicationId publicationId)
		throws RemoteException, DLibraException
	{
		InputFilter in = new PublicationFilter(null, publicationId)
				.setEditionState(Edition.ALL_STATES - Edition.PERMANENT_DELETED);
		OutputFilter out = new OutputFilter(Edition.class);
		Collection<DLObject> results = publicationManager.getObjects(in, out)
				.getResults();
		if (results.isEmpty()) {
			throw new DLibraException(null, "No editions for publication "
					+ publicationId) {

				private static final long serialVersionUID = -7493352685629908419L;
				// TODO probably another exception would fit better here
			};
		}
		Edition result = null;
		for (DLObject object : results) {
			Edition edition = (Edition) object;
			if (result == null
					|| edition.getCreationDate()
							.after(result.getCreationDate())) {
				result = edition;
			}
		}
		return result;
	}


	public Edition getEdition(EditionId editionId)
		throws RemoteException, DLibraException
	{
		InputFilter in = new EditionFilter(editionId);
		OutputFilter out = new OutputFilter(Edition.class);
		return (Edition) publicationManager.getObjects(in, out).getResult();
	}


	/**
	 * Returns id of the most recently created edition of the RO version (publication).
	 * @param publicationId Id of the publication (RO version).
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException in case no edition is found
	 */
	public EditionId getLastEditionId(PublicationId publicationId)
		throws RemoteException, DLibraException
	{
		return (EditionId) getLastEdition(publicationId).getId();
	}


	/**
	 * First retrieves the publication, then looks for edition.
	 * @param groupPublicationName
	 * @param publicationName
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public EditionId getLastEditionId(String groupPublicationName,
			String publicationName)
		throws RemoteException, DLibraException
	{
		PublicationId publicationId = dLibra.getPublicationsHelper()
				.getPublicationId(groupPublicationName, publicationName);
		EditionId editionId = getLastEditionId(publicationId);
		return editionId;
	}


	public Edition getLastEdition(String groupPublicationName,
			String publicationName)
		throws RemoteException, DLibraException
	{
		PublicationId publicationId = dLibra.getPublicationsHelper()
				.getPublicationId(groupPublicationName, publicationName);
		Edition edition = getLastEdition(publicationId);
		return edition;
	}


	/**
	 * @param publicationName
	 * @param publicationId
	 * @param createdVersion
	 * @return 
	 * @throws DLibraException
	 * @throws AccessDeniedException
	 * @throws IdNotFoundException
	 * @throws RemoteException
	 * @throws IllegalArgumentException
	 */
	EditionId createEdition(String editionName, PublicationId publicationId,
			VersionId[] versionIds)
		throws DLibraException, AccessDeniedException, IdNotFoundException,
		RemoteException, IllegalArgumentException
	{
		Edition edition = new Edition(null, publicationId, false);
		edition.setName(editionName);
		return publicationManager.createEdition(edition, versionIds);
	}


	/**
	 * Creates an edition for a given publication, copying all files associated with most
	 * recently created edition for this publication.
	 * @param editionName
	 * @param groupPublicationName
	 * @param publicationName
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public EditionId createEdition(String editionName,
			String groupPublicationName, String publicationName)
		throws RemoteException, DLibraException
	{
		PublicationId publicationId = dLibra.getPublicationsHelper()
				.getPublicationId(groupPublicationName, publicationName);
		EditionId prevEditionId = getLastEditionId(publicationId);

		InputFilter in = new EditionFilter(prevEditionId);
		OutputFilter out = new OutputFilter(VersionId.class);
		List<Id> ids = (List<Id>) publicationManager.getObjects(in, out)
				.getResultIds();

		List<VersionId> versionIds = new ArrayList<VersionId>(ids.size());
		for (Id id : ids) {
			versionIds.add((VersionId) id);
		}
		return createEdition(editionName, publicationId,
			versionIds.toArray(new VersionId[] {}));
	}


	/**
	 * Returns a list of all editions for a given publication, sorted by date of creation, oldest first.
	 * @param groupPublicationName
	 * @param publicationName
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public SortedSet<Edition> getEditionList(String groupPublicationName,
			String publicationName)
		throws RemoteException, DLibraException
	{
		TreeSet<Edition> result = new TreeSet<Edition>(
				new EditionCreatedComparator());

		PublicationId publicationId = dLibra.getPublicationsHelper()
				.getPublicationId(groupPublicationName, publicationName);
		InputFilter in = new PublicationFilter(null, publicationId)
				.setEditionState(Edition.ALL_STATES - Edition.PERMANENT_DELETED);
		OutputFilter out = new OutputFilter(Edition.class);
		Collection<DLObject> editions = publicationManager.getObjects(in, out)
				.getResults();

		for (DLObject object : editions) {
			Edition edition = (Edition) object;
			result.add(edition);
		}

		return result;
	}

}
