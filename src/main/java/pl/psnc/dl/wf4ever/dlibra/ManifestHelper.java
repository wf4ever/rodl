package pl.psnc.dl.wf4ever.dlibra;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.RdfBuilder;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.PublicationInfo;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.DCTerms;

public class ManifestHelper
{

	private final static Logger logger = Logger.getLogger(ManifestHelper.class);

	private DLibraDataSource dLibra;


	public ManifestHelper(DLibraDataSource dLibraDataSource)
	{
		this.dLibra = dLibraDataSource;
	}


	public InputStream getManifest(EditionId editionId)
		throws IOException, DLibraException
	{
		return dLibra.getFilesHelper().getFileContents(editionId,
			Constants.MANIFEST_FILENAME);
	}


	public void updateManifest(String versionUri, String groupPublicationName,
			String publicationName, InputStream manifest)
		throws DLibraException, IOException, TransformerException,
		JenaException, IncorrectManifestException
	{

		regenerateManifest(groupPublicationName, publicationName, versionUri,
			manifest);

		manifest.reset();
		dLibra.getAttributesHelper().updateMetadataAttributes(
			groupPublicationName, publicationName, manifest);

	}


	/**
	 * Creates a manifest containing all the mandatory properties.
	 * @param uri Resource (RO version) URI
	 * @param identifier dcterms:identifier
	 * @param creator dcterms:creator
	 * @param title dcterms:title
	 * @param description dcterms:description
	 * @param version oxds:currentVersion
	 * @param created dcterms:created
	 * @return Serialized manifest
	 * @throws TransformerException when serializing the manifest ends with error
	 */
	InputStream createInitialManifest(String uri, String identifier,
			String creator, String title, String description, String version,
			Literal created)
		throws TransformerException
	{
		return RdfBuilder.getResourceAsStream(createInitialManifestResource(
			uri, identifier, creator, title, description, version, created));
	}


	private Resource createInitialManifestResource(String uri,
			String identifier, String creator, String title,
			String description, String version, Literal created)
	{
		Resource resource = RdfBuilder.createResource(uri);

		resource.addLiteral(DCTerms.identifier, identifier);
		resource.addLiteral(DCTerms.creator, creator);
		resource.addLiteral(DCTerms.title, title);
		resource.addLiteral(DCTerms.description, description);
		resource.addLiteral(RdfBuilder.OXDS_CURRENT_VERSION, version);
		resource.addProperty(DCTerms.created, created);
		return resource;
	}


	/**
	 * 
	 * @param groupPublicationName RO name
	 * @param publicationName version name
	 * @param versionURI RO version URI
	 * @param baseManifest Manifest from which data should be taken with the same URI as new one.
	 * @throws DLibraException
	 * @throws IOException
	 * @throws TransformerException
	 * @throws JenaException thrown on malformed base manifest
	 * @throws IncorrectManifestException thrown if a property is missing
	 */
	public void regenerateManifest(String groupPublicationName,
			String publicationName, String versionURI, InputStream baseManifest)
		throws JenaException, DLibraException, IOException,
		TransformerException, IncorrectManifestException
	{
		regenerateManifest(groupPublicationName, publicationName, versionURI,
			baseManifest, versionURI);
	}


	/**
	 * 
	 * @param groupPublicationName RO name
	 * @param publicationName version name
	 * @param versionURI RO version URI
	 * @param manifest Manifest from which data should be taken.
	 * @param baseVersionURI URI to use from base manifest
	 * @throws DLibraException
	 * @throws IOException
	 * @throws TransformerException
	 * @throws JenaException thrown on malformed base manifest
	 * @throws IncorrectManifestException thrown if a property is missing
	 */
	void regenerateManifest(String groupPublicationName,
			String publicationName, String versionURI, InputStream manifest,
			String baseVersionURI)
		throws DLibraException, IOException, TransformerException,
		JenaException, IncorrectManifestException
	{
		Resource baseResource = getBaseResource(baseVersionURI, manifest);

		Resource resource = createInitialManifestResource(versionURI,
			publicationName, baseResource);

		addOptionalROSRSProperties(resource, versionURI, groupPublicationName,
			publicationName);

		addRemainingBaseManifestProperties(resource, baseResource);

		InputStream is = new ByteArrayInputStream(RdfBuilder.transformManifest(
			RdfBuilder.serializeResource(resource)).getBytes());

		// save manifest.rdf
		dLibra.getFilesHelper().createOrUpdateFile(versionURI,
			groupPublicationName, publicationName, Constants.MANIFEST_FILENAME,
			is, Constants.RDF_XML_MIME_TYPE, false);
	}


	private Resource getBaseResource(String versionUri, InputStream manifest)
		throws IncorrectManifestException
	{
		Model model = ModelFactory.createDefaultModel();
		model.read(manifest, null);

		// createResource may create new or use existing one
		Resource baseResource = model.createResource(versionUri);

		RdfBuilder.checkMandatoryProperties(baseResource);
		return baseResource;
	}


	private Resource createInitialManifestResource(String versionUri,
			String version, Resource baseResource)
	{
		String identifier = baseResource.getProperty(DCTerms.identifier)
				.getString();
		String creator = baseResource.getProperty(DCTerms.creator).getString();
		String title = baseResource.getProperty(DCTerms.title).getString();
		String description = baseResource.getProperty(DCTerms.description)
				.getString();
		Literal created = baseResource.getProperty(DCTerms.created)
				.getLiteral();

		return createInitialManifestResource(versionUri, identifier, creator,
			title, description, version, created);
	}


	private void addOptionalROSRSProperties(Resource resource,
			String versionUri, String groupPublicationName,
			String publicationName)
		throws RemoteException, DLibraException
	{
		EditionId editionId = dLibra.getEditionHelper().getLastEditionId(
			groupPublicationName, publicationName);
		List<String> list = dLibra.getFilesHelper().getFilePathsInPublication(
			editionId);

		for (int i = 0; i < list.size(); i++) {
			list.set(i, versionUri + list.get(i));
		}

		RdfBuilder.addAggregation(resource, list);

		String researchObjectUri = versionUri.substring(0,
			versionUri.lastIndexOf("/"));

		for (PublicationInfo info : dLibra.getPublicationsHelper()
				.listPublicationsInGroup(groupPublicationName)) {
			resource.addProperty(DCTerms.hasVersion, researchObjectUri + "/"
					+ info.getLabel());
		}

		Date creationDate = new Date();
		resource.addProperty(DCTerms.modified,
			RdfBuilder.createDateLiteral(creationDate));
		dLibra.getAttributesHelper().updateModifiedAttribute(
			groupPublicationName, publicationName, creationDate.toString());
	}


	private void addRemainingBaseManifestProperties(Resource resource,
			Resource baseResource)
	{

		StmtIterator iterator = baseResource.listProperties();
		for (Statement statement : iterator.toList()) {
			if (!RdfBuilder.READ_ONLY_PROPERTIES.contains(statement
					.getPredicate())
					&& !RdfBuilder.MANDATORY_PROPERTIES.contains(statement
							.getPredicate())) {
				resource.addProperty(statement.getPredicate(),
					statement.getObject());
			}
		}

	}


	/**
	 * @param versionUri
	 * @param groupPublicationName
	 * @param publicationName
	 * @param generateManifest
	 * @throws DLibraException
	 * @throws IOException
	 * @throws TransformerException
	 */
	public void regerenerateManifestSafe(String versionUri,
			String groupPublicationName, String publicationName,
			String baseVersionURI)
		throws DLibraException, IOException, TransformerException
	{
		EditionId editionId = dLibra.getEditionHelper().getLastEditionId(
			groupPublicationName, publicationName);
		try {
			regenerateManifest(groupPublicationName, publicationName,
				versionUri, getManifest(editionId), baseVersionURI);
		}
		catch (JenaException e) {
			logger.warn("Manifest stored for publication "
					+ groupPublicationName + " is malformed");
		}
		catch (IncorrectManifestException e) {
			logger.warn(String.format(
				"Manifest stored for publication %s/%s is incorrect (%s)",
				groupPublicationName, publicationName, e.getMessage()));
		}
		catch (IdNotFoundException e) {
			logger.error(String.format(
				"Manifest stored for %s/%s not found (%s)",
				groupPublicationName, publicationName, e.getMessage()));
		}
	}


	/**
	 * @param versionUri
	 * @param groupPublicationName
	 * @param publicationName
	 * @param generateManifest
	 * @throws DLibraException
	 * @throws IOException
	 * @throws TransformerException
	 */
	public void regerenerateManifestSafe(String versionUri,
			String groupPublicationName, String publicationName)
		throws DLibraException, IOException, TransformerException
	{
		regerenerateManifestSafe(versionUri, groupPublicationName,
			publicationName, versionUri);
	}
}
