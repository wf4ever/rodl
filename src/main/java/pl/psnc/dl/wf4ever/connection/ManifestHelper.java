package pl.psnc.dl.wf4ever.connection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.RdfBuilder;
import pl.psnc.dlibra.metadata.PublicationInfo;
import pl.psnc.dlibra.service.DLibraException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.DCTerms;


public class ManifestHelper
{
	private DLibraDataSource dLibra;
	
	public ManifestHelper(DLibraDataSource dLibraDataSource)
	{
		this.dLibra = dLibraDataSource;
	}


	public String getManifest(String groupPublicationName,
			String publicationName)
		throws IOException, DLibraException
	{
		InputStream fileContents = dLibra.getFilesHelper().getFileContents(groupPublicationName,
			publicationName, Constants.MANIFEST_FILENAME);
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
		JenaException
	{

		regenerateManifest(groupPublicationName, publicationName, versionUri,
			manifest);

		dLibra.getAttributesHelper().updateDlibraAttributes(versionUri, groupPublicationName,
			publicationName, manifest);

	}


	String createEmptyManifest(String uri)
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


	void regenerateManifest(String groupPublicationName,
			String publicationName, String versionUri, String baseManifest)
		throws DLibraException, IOException, TransformerException,
		JenaException
	{

		List<String> list = dLibra.getFilesHelper().getFilePathsInPublication(groupPublicationName,
			publicationName);

		for (int i = 0; i < list.size(); i++) {
			list.set(i, versionUri + list.get(i));
		}

		Resource resource = RdfBuilder.createCollection(versionUri, list);

		// add read only tags
		{
			String researchObjectUri = versionUri.substring(0,
				versionUri.lastIndexOf("/"));

			for (PublicationInfo info : dLibra.getPublicationsHelper().listPublicationsInGroup(groupPublicationName)) {
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
		dLibra.getFilesHelper().createOrUpdateFile(versionUri, groupPublicationName, publicationName,
			Constants.MANIFEST_FILENAME, is, Constants.RDF_XML_MIME_TYPE, false);
	}

}
