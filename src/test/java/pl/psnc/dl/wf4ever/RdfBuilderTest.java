//package pl.psnc.dl.wf4ever;
//
//import java.io.ByteArrayInputStream;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//import javax.xml.transform.TransformerException;
//
//import junit.framework.Assert;
//
//import org.junit.Test;
//
//import com.hp.hpl.jena.rdf.model.Model;
//import com.hp.hpl.jena.rdf.model.ModelFactory;
//import com.hp.hpl.jena.rdf.model.Resource;
//import com.hp.hpl.jena.rdf.model.StmtIterator;
//import com.hp.hpl.jena.shared.BadURIException;
//
///**
// * @author nowakm
// *
// */
//public class RdfBuilderTest
//{
//
//	private static final String MALFORMED_URI_PREFIX = "asdf asfafs";
//
//	private final static URI objectUri = URI.create("http://example.org/object/");
//
//
//	@Test
//	public void checkObjectUri()
//		throws TransformerException
//	{
//		String collection = RdfBuilder.serializeResource(RdfBuilder
//				.createCollection(objectUri, Collections.<URI> emptyList()));
//
//		Model model = ModelFactory.createDefaultModel();
//		model.read(new ByteArrayInputStream(collection.getBytes()), null);
//
//		Assert.assertNotNull(model.getResource(objectUri.toString()));
//	}
//
//
//	@Test
//	public void checkAggregatedObjectsUri()
//		throws TransformerException
//	{
//		List<String> uris = new ArrayList<String>();
//		for (int i = 1; i < 10; i++) {
//			uris.add(objectUri + i);
//		}
//
//		String collection = RdfBuilder.serializeResource(RdfBuilder
//				.createCollection(objectUri, uris));
//
//		Model model = ModelFactory.createDefaultModel();
//		model.read(new ByteArrayInputStream(collection.getBytes()), null);
//
//		Resource resource = model.getResource(objectUri);
//		Assert.assertNotNull(resource);
//
//		StmtIterator aggregated = resource
//				.listProperties(RdfBuilder.AGGREGATES);
//		Assert.assertEquals(uris.size(), aggregated.toList().size());
//
//		for (String uri : uris) {
//			Assert.assertNotNull(model.getResource(uri));
//		}
//
//	}
//
//
//	@Test
//	public void checEmptyObject()
//		throws TransformerException
//	{
//		List<String> uris = new ArrayList<String>();
//
//		String collection = RdfBuilder.serializeResource(RdfBuilder
//				.createCollection(objectUri, uris));
//
//		Model model = ModelFactory.createDefaultModel();
//		model.read(new ByteArrayInputStream(collection.getBytes()), null);
//
//		Resource resource = model.getResource(objectUri);
//		Assert.assertNotNull(resource);
//
//		StmtIterator aggregated = resource
//				.listProperties(RdfBuilder.AGGREGATES);
//		Assert.assertEquals(uris.size(), aggregated.toList().size());
//
//		for (String uri : uris) {
//			Assert.assertNotNull(model.getResource(uri));
//		}
//
//	}
//
//
//	@Test(expected = BadURIException.class)
//	public void checkAggregatedObjectsWithMalformedUri()
//		throws TransformerException
//	{
//		List<String> uris = new ArrayList<String>();
//		for (int i = 1; i < 10; i++) {
//			uris.add(MALFORMED_URI_PREFIX + objectUri + i);
//		}
//
//		RdfBuilder.serializeResource(RdfBuilder.createCollection(objectUri,
//			uris));
//
//	}
//
//
//	@Test(expected = BadURIException.class)
//	public void checkObjectWithMalformedUri()
//		throws TransformerException
//	{
//		RdfBuilder
//				.serializeResource(RdfBuilder.createCollection(
//					MALFORMED_URI_PREFIX + objectUri,
//					Collections.<String> emptyList()));
//	}
//}
