package pl.psnc.dl.wf4ever;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import pl.psnc.dl.wf4ever.dlibra.IncorrectManifestException;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * 
 * @author nowakm
 *
 */
public class RdfBuilder
{

	//	private final static Logger logger = Logger.getLogger(RdfBuilder.class);
	private static Model model = ModelFactory.createDefaultModel();

	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");

	private static final String DateTimeFormatXSDUri = "http://www.w3.org/2001/XMLSchema#dateTime";

	private static final String ORE_NAMESPACE = "http://www.openarchives.org/ore/terms/";

	private static final String ORE_PREFIX = "ore";

	private static final String OXDS_NAMESPACE = "http://vocab.ox.ac.uk/dataset/schema#";

	private static final String OXDS_PREFIX = "oxds";

	private static final Resource AGGREGATION = model
			.createResource(ORE_NAMESPACE + "Aggregation");

	private static final String TYPE = "RDF/XML";

	private static final PrefixMapping StandardNamespaces = PrefixMapping.Factory
			.create().setNsPrefix(ORE_PREFIX, ORE_NAMESPACE)
			.setNsPrefix(OXDS_PREFIX, OXDS_NAMESPACE)
			.setNsPrefix("dcterms", DCTerms.NS).lock();

	public static final Property AGGREGATES = model
			.createProperty(ORE_NAMESPACE + "aggregates");

	public static final Property OXDS_CURRENT_VERSION = model
			.createProperty(OXDS_NAMESPACE + "currentVersion");

	/**
	 * Manifest properties that cannot be changed by users.
	 */
	public static final List<Property> READ_ONLY_PROPERTIES = Arrays.asList(
		DCTerms.identifier, AGGREGATES, DCTerms.hasVersion,
		OXDS_CURRENT_VERSION, RDF.type, DCTerms.modified, DCTerms.created);

	/**
	 * Properties that have to be present in the manifest.
	 */
	public static final List<Property> MANDATORY_PROPERTIES = Arrays.asList(
		DCTerms.identifier, DCTerms.creator, DCTerms.title,
		DCTerms.description, OXDS_CURRENT_VERSION, DCTerms.created);

	static {
		model.setNsPrefixes(StandardNamespaces);
	}


	/**
	 * No instances allowed
	 */
	private RdfBuilder()
	{
		// nop
	}


	public static Resource createCollection(String collectionUri,
			List<String> links)
	{
		model.removeAll();
		Resource collection = model.createResource(collectionUri);

		addAggregation(collection, links);

		return collection;
	}


	public static void addAggregation(Resource resource, List<String> links)
	{
		resource.addProperty(RDF.type, AGGREGATION);
		for (String uri : links) {
			Resource object = model.createResource(uri);
			resource.addProperty(AGGREGATES, object);

		}
	}

	/**
	 * Returns rdf/xml serialization of specified Resource in String. 
	 */
	public static String serializeResource(Resource resource)
		throws TransformerException
	{
		String output;
		OutputStream out = new ByteArrayOutputStream();

		resource.getModel().write(out, TYPE);
		try {
			output = new String(out.toString().getBytes(), "UTF8");
		}
		catch (UnsupportedEncodingException e) {
			output = out.toString();
		}

		return output;
	}
	
	/**
	 * Returns rdf/xml serialization of specified Resource in String. 
	 */
	public static InputStream getResourceAsStream(Resource resource)
		throws TransformerException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		resource.getModel().write(out, TYPE);

		return new ByteArrayInputStream(out.toByteArray());
	}
	


	public static Literal createDateLiteral(Date date)
	{

		String text = sdf.format(date);
		return model.createTypedLiteral(text, DateTimeFormatXSDUri);
	}


	public static Resource createResource(String uri)
	{
		model.removeAll();
		return model.createResource(uri);
	}


	/**
	 * Performs xslt transformation of manifest.rdf serialized to String and returns transformed manifest as String.
	 */
	public static String transformManifest(String manifest)
		throws TransformerException
	{
		TransformerFactory tFactory = TransformerFactory.newInstance();

		InputStream inputStream = RdfBuilder.class.getClassLoader()
				.getResourceAsStream("rdf-order.xsl");

		Transformer transformer = tFactory.newTransformer(new StreamSource(
				inputStream));

		StreamSource source = new StreamSource(new StringReader(manifest));
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		transformer.transform(source, result);

		return sw.toString();
	}


	public static void checkMandatoryProperties(Resource resource)
		throws IncorrectManifestException
	{
		for (Property property : MANDATORY_PROPERTIES) {
			if (!resource.hasProperty(property)) {
				throw new IncorrectManifestException(
						"Resource does not have a mandatory property: "
								+ property + " resource: " + resource.toString());
			}
		}
	}

}
