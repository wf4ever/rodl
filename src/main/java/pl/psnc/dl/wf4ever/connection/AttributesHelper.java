/**
 * 
 */
package pl.psnc.dl.wf4ever.connection;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import pl.psnc.dlibra.common.DLObject;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeValueId;
import pl.psnc.dlibra.metadata.attributes.AttributeValueManager;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;

/**
 * @author piotrhol
 *
 */
public class AttributesHelper
{

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
	 * @param versionUri
	 * @param groupPublicationName
	 * @param publicationName
	 * @param manifest
	 * @throws DLibraException 
	 * @throws RemoteException 
	 */
	public void updateDlibraAttributes(String versionUri,
			String groupPublicationName, String publicationName, String manifest)
		throws RemoteException, DLibraException
	{
//		PublicationId publicationId = dLibra.getPublicationsHelper()
//				.getPublicationId(groupPublicationName, publicationName);
//		EditionId editionId = dLibra.getFilesHelper().getEditionId(
//			publicationId);

//		Model model = ModelFactory.createDefaultModel();
//		model.read(new ByteArrayInputStream(manifest.getBytes()), null);
//
//		StmtIterator iterator = model.listStatements();
//		for (Statement statement : iterator.toList()) {
//			if (statement.getPredicate().equals(DCTerms.identifier)) {
//			}
//			else if (statement.getPredicate().equals(DCTerms.creator)) {
//				AttributeValue value = new AttributeValue(null);
//				value.setValue(statement.getString());
//				createAttributeValue(value);
//			}
//			else if (statement.getPredicate().equals(DCTerms.title)) {
//			}
//			else if (statement.getPredicate().equals(DCTerms.description)) {
//			}
//		}
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
}
