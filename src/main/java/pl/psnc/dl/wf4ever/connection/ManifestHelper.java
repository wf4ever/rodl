package pl.psnc.dl.wf4ever.connection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.RdfBuilder;
import pl.psnc.dlibra.metadata.PublicationInfo;
import pl.psnc.dlibra.service.DLibraException;

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

	@SuppressWarnings("unused")
	private final static Logger logger = Logger.getLogger(ManifestHelper.class);

	private DLibraDataSource dLibra;


	public ManifestHelper(DLibraDataSource dLibraDataSource)
	{
		this.dLibra = dLibraDataSource;
	}


	public String getManifest(String groupPublicationName,
			String publicationName)
		throws IOException, DLibraException
	{
		InputStream fileContents = dLibra.getFilesHelper().getFileContents(
			groupPublicationName, publicationName, Constants.MANIFEST_FILENAME);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[DLibraDataSource.BUFFER_SIZE];
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


	public void updateManifest(String versionUri, String groupPublicationName,
			String publicationName, String manifest)
		throws DLibraException, IOException, TransformerException,
		JenaException, IncorrectManifestException
	{

		regenerateManifest(groupPublicationName, publicationName, versionUri,
			manifest);

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
	String createInitialManifest(String uri, String identifier, String creator,
			String title, String description, String version, Literal created)
		throws TransformerException
	{
		return RdfBuilder.serializeResource(createInitialManifestResource(uri,
			identifier, creator, title, description, version, created));
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
	 * @param versionUri RO version URI
	 * @param baseManifest Has to have the same URI as the new one.
	 * @throws DLibraException
	 * @throws IOException
	 * @throws TransformerException
	 * @throws JenaException thrown on malformed base manifest
	 * @throws IncorrectManifestException thrown if a property is missing
	 */
	void regenerateManifest(String groupPublicationName,
			String publicationName, String versionUri, String baseManifest)
		throws DLibraException, IOException, TransformerException,
		JenaException, IncorrectManifestException
	{
		Resource baseResource = getBaseResource(versionUri, baseManifest);

		Resource resource = createInitialManifestResource(versionUri,
			publicationName, baseResource);

		addOptionalROSRSProperties(resource, versionUri, groupPublicationName,
			publicationName);

		addRemainingBaseManifestProperties(resource, baseResource);

		InputStream is = new ByteArrayInputStream(RdfBuilder.transformManifest(
			RdfBuilder.serializeResource(resource)).getBytes());

		// save manifest.rdf
		dLibra.getFilesHelper().createOrUpdateFile(versionUri,
			groupPublicationName, publicationName, Constants.MANIFEST_FILENAME,
			is, Constants.RDF_XML_MIME_TYPE, false);
	}


	private Resource getBaseResource(String versionUri, String baseManifest)
		throws IncorrectManifestException
	{
		Model model = ModelFactory.createDefaultModel();
		model.read(new ByteArrayInputStream(baseManifest.getBytes()), null);

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
		List<String> list = dLibra.getFilesHelper().getFilePathsInPublication(
			groupPublicationName, publicationName);

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
			groupPublicationName, publicationName,
			creationDate.toString());
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

}
