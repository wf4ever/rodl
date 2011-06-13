/**
 * 
 */
package pl.psnc.dl.wf4ever.connection;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.common.CollectionResult;
import pl.psnc.dlibra.common.DLObject;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.Language;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeId;
import pl.psnc.dlibra.metadata.attributes.AttributeInfo;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeValueId;
import pl.psnc.dlibra.metadata.attributes.AttributeValueManager;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.DuplicatedValueException;
import pl.psnc.dlibra.service.IdNotFoundException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;

/**
 * @author piotrhol
 *
 */
public class AttributesHelper
{

	//TODO: change into a configuration file
	public static final String CREATOR_RDF_NAME = "Creator";

	public static final String TITLE_RDF_NAME = "Title";

	public static final String DESCRIPTION_RDF_NAME = "Description";

	public static final String SOURCE_RDF_NAME = "Source";

	public static final String CREATED_RDF_NAME = "Created";

	public static final String MODIFIED_RDF_NAME = "Modified";

	public static final String ATTRIBUTE_LANGUAGE = Language.UNIVERSAL;

	private final static Logger logger = Logger
			.getLogger(AttributesHelper.class);

	private DLibraDataSource dLibra;


	public AttributesHelper(DLibraDataSource dLibraDataSource)
	{
		this.dLibra = dLibraDataSource;
	}


	/**
	 * Updates dLibra attributes based on a manifest, see 
	 * http://www.wf4ever-project.org/wiki/display/docs/Research+Objects+Store+and+Retrieve+Service#ResearchObjectsStoreandRetrieveService-Metadata
	 * This method does not modify resource links, so it doesn't need to be called when
	 * resources themselves are deleted/added, etc. 
	 * @param groupPublicationName
	 * @param publicationName
	 * @param manifest
	 * @throws DLibraException 
	 * @throws RemoteException 
	 */
	public void updateMetadataAttributes(String groupPublicationName,
			String publicationName, String manifest)
		throws RemoteException, DLibraException
	{
		AttributeValueSet avs = getAttributeValueSet(groupPublicationName,
			publicationName);

		Model model = ModelFactory.createDefaultModel();
		model.read(new ByteArrayInputStream(manifest.getBytes()), null);

		Set<Property> predicates = new HashSet<Property>();

		StmtIterator iterator = model.listStatements();
		for (Statement statement : iterator.toList()) {
			if (statement.getPredicate().equals(DCTerms.identifier)) {
				if (!statement.getString().equals(groupPublicationName)) {
					logger.warn(String
							.format(
								"dcterms:identifier is '%s' but group publication name was '%s', ignoring dcterms:identifier",
								statement.getString(), groupPublicationName));
				}
			}
			else if (statement.getPredicate().equals(DCTerms.creator)) {
				updateAttribute(avs, CREATOR_RDF_NAME, statement.getString(),
					predicates.contains(statement.getPredicate()));
			}
			else if (statement.getPredicate().equals(DCTerms.title)) {
				updateAttribute(avs, TITLE_RDF_NAME, statement.getString(),
					predicates.contains(statement.getPredicate()));
			}
			else if (statement.getPredicate().equals(DCTerms.description)) {
				updateAttribute(avs, DESCRIPTION_RDF_NAME,
					statement.getString(),
					predicates.contains(statement.getPredicate()));
			}
			else if (statement.getPredicate().equals(DCTerms.source)) {
				updateAttribute(avs, SOURCE_RDF_NAME, statement.getResource().getURI(),
					predicates.contains(statement.getPredicate()));
			}
			predicates.add(statement.getPredicate());
		}

		commitAttributeValueSet(avs);
	}


	/**
	 * Update the creation date of RO version.
	 * @param groupPublicationName RO name
	 * @param publicationName version name
	 * @param date creation date
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public void updateCreatedAttribute(String groupPublicationName,
			String publicationName, String date)
		throws RemoteException, DLibraException
	{
		AttributeValueSet avs = getAttributeValueSet(groupPublicationName,
			publicationName);
		updateAttribute(avs, CREATED_RDF_NAME, date, false);
		commitAttributeValueSet(avs);
	}


	/**
	 * Update the modification date of RO version.
	 * @param groupPublicationName RO name
	 * @param publicationName version name
	 * @param date modification date
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public void updateModifiedAttribute(String groupPublicationName,
			String publicationName, String date)
		throws RemoteException, DLibraException
	{
		AttributeValueSet avs = getAttributeValueSet(groupPublicationName,
			publicationName);
		updateAttribute(avs, MODIFIED_RDF_NAME, date, false);
		commitAttributeValueSet(avs);
	}


	private void updateAttribute(AttributeValueSet avs,
			String attributeRdfName, String value, boolean keepOld)
		throws IdNotFoundException, RemoteException, DLibraException
	{
		if (value.isEmpty()) {
			logger.warn(String.format("Ignoring empty value for attribute %s",
				attributeRdfName));
			return;
		}

		// find the attribute
		AttributeInfo attributeInfo = null;
		CollectionResult result = dLibra
				.getMetadataServer()
				.getAttributeManager()
				.getObjects(
					new AttributeFilter((AttributeId) null).setRDFNames(Arrays
							.asList(attributeRdfName)),
					new OutputFilter(AttributeInfo.class));
		if (result.getResultsCount() != 1) {
			logger.error(String.format(
				"Found %d attributes with RDF name '%s'",
				result.getResultsCount(), attributeRdfName));
			return;
		}
		else {
			attributeInfo = (AttributeInfo) result.getResultInfo();
		}

		// create attribute value
		AttributeValue attValue = new AttributeValue(null);
		attValue.setAttributeId(attributeInfo.getId());
		attValue.setValue(value);
		attValue.setLanguageName(ATTRIBUTE_LANGUAGE);
		attValue = createAttributeValue(attValue);

		// wrap in a list
		List<AbstractAttributeValue> attValues = (List<AbstractAttributeValue>) (keepOld
				&& avs.getAttributeIds().contains(attributeInfo.getId()) ? avs
				.getAttributeValues(attributeInfo.getId())
				: new ArrayList<AbstractAttributeValue>());
		attValues.add(attValue);

		// update attribute value set
		avs.setDirectAttributeValues(attributeInfo.getId(), ATTRIBUTE_LANGUAGE,
			attValues);

		logger.debug(String.format("Updated attribute %s (%d) with value %s",
			attributeInfo.getRDFName(), attributeInfo.getId().getId(),
			attValue.getValue()));
	}


	/**
	 * Adds attribute value specified by text value. Chooses one from system
	 * or creates a new one if such value does not exist.
	 * @param value Text specifying value.
	 * @return attribute value
	 * @throws RemoteException in case remote server exception occurred.
	 * @throws IdNotFoundException in case there was a problem with identifiers.
	 * @throws DLibraException
	 * @throws RemoteException
	 * @throws OperationFailedException
	 * @throws DuplicatedValueException when new group cannot be created
	 * because of name conflict (may occur when concurrent modifications are
	 * performed).
	 * @throws AccessDeniedException when there is no access
	 * to perform adding new group operation.
	 */
	private AttributeValue createAttributeValue(final AttributeValue value)
		throws RemoteException, DLibraException
	{
		AttributeValueManager attributeValueManager = dLibra
				.getMetadataServer().getAttributeValueManager();
		List<AttributeValue> groupsWithValue = new ArrayList<AttributeValue>();
		for (DLObject obj : attributeValueManager.getObjects(
			new AttributeValueFilter(value.getAttributeId()).setValue(
				value.getValue(), true)
					.setLanguageName(value.getLanguageName()),
			new OutputFilter(AttributeValue.class)).getResults())
			groupsWithValue.add((AttributeValue) obj);
		if (groupsWithValue.isEmpty()) {
			logger.debug("No groups with value " + value);
			AttributeValueId id = attributeValueManager
					.addAttributeValue(value);
			value.setId(id);
			return value;
		}
		else if (groupsWithValue.size() == 1) {
			AttributeValue group = groupsWithValue.get(0);
			if (group != null) {
				return getAttributeValueFromGroup(value.getValue(), group,
					value.getLanguageName());
			}
		}
		else {
			//			return getGroupForValue(value, groupsWithValue);
		}
		return null;
	}


	private AttributeValue getAttributeValueFromGroup(String value,
			AttributeValue group, String selLang)
		throws RemoteException, IdNotFoundException, DLibraException

	{
		Collection<DLObject> groupValues = dLibra
				.getMetadataServer()
				.getAttributeValueManager()
				.getObjects(new AttributeValueFilter(null, group.getId()),
					new OutputFilter(AttributeValue.class)).getResults();
		AttributeValue foundValue = null;
		boolean eqIgnCase = false;
		for (Iterator<DLObject> iter = groupValues.iterator(); iter.hasNext();) {
			AttributeValue element = (AttributeValue) iter.next();
			element.setLanguageName(selLang);
			if (element.getValue().equalsIgnoreCase(value)) {
				eqIgnCase = true;
			}
			if (element.getValue().equals(value)) {
				foundValue = element;
				break;
			}
		}
		if (foundValue != null) {
			return foundValue;
		}
		if (eqIgnCase) {
			//FIXME: what to throw?
		}
		throw new IdNotFoundException("Could not find value in group. Value: "
				+ value + " Group id: " + group.getBaseId()
				+ " Language name: " + selLang);
	}


	private AttributeValueSet getAttributeValueSet(String groupPublicationName,
			String publicationName)
		throws RemoteException, DLibraException, IdNotFoundException
	{
		PublicationId publicationId = dLibra.getPublicationsHelper()
				.getPublicationId(groupPublicationName, publicationName);
		EditionId editionId = dLibra.getFilesHelper().getEditionId(
			publicationId);

		AttributeValueSet avs = dLibra.getMetadataServer()
				.getElementMetadataManager()
				.getAttributeValueSet(editionId, AttributeValue.AV_ASSOC_ALL);
		return avs;
	}


	private void commitAttributeValueSet(AttributeValueSet avs)
		throws RemoteException, IdNotFoundException, AccessDeniedException,
		DLibraException
	{
		dLibra.getMetadataServer().getElementMetadataManager()
				.setAttributeValueSet(avs);
	}

}
