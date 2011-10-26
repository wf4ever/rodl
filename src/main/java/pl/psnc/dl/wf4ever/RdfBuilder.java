package pl.psnc.dl.wf4ever;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import javax.xml.transform.TransformerException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * 
 * @author nowakm
 * 
 */
public class RdfBuilder {

	// private final static Logger logger = Logger.getLogger(RdfBuilder.class);
	private static Model model = ModelFactory.createDefaultModel();

	private static final String ORE_NAMESPACE = "http://www.openarchives.org/ore/terms/";

	private static final Resource AGGREGATION = model.createResource(ORE_NAMESPACE + "Aggregation");

	private static final String TYPE = "RDF/XML";

	public static final Property AGGREGATES = model.createProperty(ORE_NAMESPACE + "aggregates");

	/**
	 * No instances allowed
	 */
	private RdfBuilder() {
		// nop
	}

	public static Resource createCollection(URI uri, List<URI> links) {
		model.removeAll();
		Resource collection = model.createResource(uri.toString());

		addAggregation(collection, links);

		return collection;
	}

	public static void addAggregation(Resource resource, List<URI> links) {
		resource.addProperty(RDF.type, AGGREGATION);
		for (URI uri : links) {
			Resource object = model.createResource(uri.toString());
			resource.addProperty(AGGREGATES, object);

		}
	}

	/**
	 * Returns rdf/xml serialization of specified Resource in String.
	 */
	public static String serializeResource(Resource resource) throws TransformerException {
		String output;
		OutputStream out = new ByteArrayOutputStream();

		resource.getModel().write(out, TYPE);
		try {
			output = new String(out.toString().getBytes(), "UTF8");
		} catch (UnsupportedEncodingException e) {
			output = out.toString();
		}

		return output;
	}

}
