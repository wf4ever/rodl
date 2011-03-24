package pl.psnc.dl.wf4ever;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

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

	private final static Logger logger = Logger.getLogger(RdfBuilder.class);

	public static final String DateFormatXSDUri = "http://www.w3.org/2001/XMLSchema#date";

	private static Model model = ModelFactory.createDefaultModel();

	private static final String ORE_NAMESPACE = "http://www.openarchives.org/ore/terms/";

	private static final String ORE_PREFIX = "ore";

	private static final String OXDS_NAMESPACE = "http://vocab.ox.ac.uk/dataset/schema#";

	private static final String OXDS_PREFIX = "oxds";

	private static final Resource AGGREGATION = model
			.createResource(ORE_NAMESPACE + "Aggregation");

	public static final Property AGGREGATES = model
			.createProperty(ORE_NAMESPACE + "aggregates");

	private static final String TYPE = "RDF/XML";

	private static final PrefixMapping StandardNamespaces = PrefixMapping.Factory
			.create().setNsPrefix(ORE_PREFIX, ORE_NAMESPACE)
			.setNsPrefix(OXDS_PREFIX, OXDS_NAMESPACE)
			.setNsPrefix("dcterms", DCTerms.NS).lock();

	public static final Property OXDS_CURRENT_VERSION = model
			.createProperty(OXDS_NAMESPACE + "currentVersion");

	public static final List<Property> READ_ONLY_PROPERTIES;

	static {
		model.setNsPrefixes(StandardNamespaces);
		READ_ONLY_PROPERTIES = new ArrayList<Property>();
		READ_ONLY_PROPERTIES.add(AGGREGATES);
		READ_ONLY_PROPERTIES.add(DCTerms.hasVersion);
		READ_ONLY_PROPERTIES.add(OXDS_CURRENT_VERSION);
		READ_ONLY_PROPERTIES.add(DCTerms.modified);
		READ_ONLY_PROPERTIES.add(RDF.type);
		//		READ_ONLY_PROPERTIES.add(DCTerms.created);
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

		collection.addProperty(RDF.type, AGGREGATION);
		for (String uri : links) {
			Resource object = model.createResource(uri);
			collection.addProperty(AGGREGATES, object);

		}

		return collection;
	}


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

		return transform(output);
	}


	private static String transform(String xml)
		throws TransformerException
	{
		TransformerFactory tFactory = TransformerFactory.newInstance();

		InputStream inputStream = RdfBuilder.class.getClassLoader()
				.getResourceAsStream("rdf-order.xsl");

		Transformer transformer = tFactory.newTransformer(new StreamSource(
				inputStream));

		StreamSource source = new StreamSource(new StringReader(xml));
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		transformer.transform(source, result);

		return sw.toString();
	}


	public static Resource createResource(String uri)
	{
		model.removeAll();
		return model.createResource(uri);
	}

}
