package pl.psnc.dl.wf4ever.model.AO;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

/**
 * Test class for AO.Annotation model.
 * 
 * @author pejot
 * 
 */
public class AnnotationTest extends BaseTest {

    private ResearchObject ro;
    private URI annotationUri = URI.create("http://www.example.com/annotationUri/");


    @Override
    @Before
    public void setUp()
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        super.setUp();
        ro = builder.buildResearchObject(URI.create(("http://www.example.com/ROs/ro/")));
    }


    @Test
    public void testAnnotation() {
        Annotation annotation = new Annotation(userProfile, dataset, useTransactions, ro, annotationUri);
        Assert.assertEquals(annotation.getResearchObject(), ro);
        Assert.assertEquals(annotation.getUri(), annotationUri);

        Thing body = new Thing(userProfile, URI.create("http://www.example.com/body/"));
        Set<Thing> annotated = new HashSet<Thing>();
        annotated.add(ro.getManifest());

        annotation = new Annotation(userProfile, ro, annotationUri, body, annotated);
        Assert.assertEquals(annotation.getUri(), annotationUri);
        Assert.assertEquals(annotation.getBody(), body);
        Assert.assertEquals(annotation.getAnnotated(), annotated);
        Assert.assertEquals(annotation.getResearchObject(), ro);

        annotation = new Annotation(userProfile, ro, annotationUri, body, ro.getManifest());
        Assert.assertEquals(annotation.getUri(), annotationUri);
        Assert.assertEquals(annotation.getBody(), body);
        Assert.assertEquals(annotation.getAnnotated().size(), 1);
        Assert.assertEquals(annotation.getAnnotated().iterator().next(), ro.getManifest());
        Assert.assertEquals(annotation.getResearchObject(), ro);
    }


    /*
    @Test
    public void testCreate()
            throws BadRequestException, IOException {
        InputStream annotationDescriptionInputStream = getClass().getClassLoader().getResourceAsStream(
            "singleFiles/annotationDescription.rdf");
        InputStream resourceInputStream = getClass().getClassLoader().getResourceAsStream("singleFiles/file1.txt");
        InputStream annotationBodyInputStream = getClass().getClassLoader()
                .getResourceAsStream("singleFiles/file2.txt");
        ro.aggregate("http://example.com/ROs/ro_id/foo/bar.txt", resourceInputStream, null);
        ro.aggregate("http://example.com/external.txt", annotationBodyInputStream, null);
        Annotation annotation = Annotation.create(builder, ro, annotationUri, annotationDescriptionInputStream);
    }
    */

    @Test
    public void testIsSpecialResource() {
        Annotation ordinaryAnnotation1 = builder.buildAnnotation(ro,
            URI.create("http://www.example.com/ROs/ro/annotations/ordinaryAnnotation1"),
            builder.buildThing(URI.create("http://www.example.com/ROs/ro/.ro/manifest.rdf/ordinary")),
            new HashSet<Thing>(Arrays.asList(ro)));

        Annotation ordinaryAnnotation2 = builder.buildAnnotation(ro,
            URI.create("http://www.example.com/ROs/ro/annotations/ordinaryAnnotation1"),
            builder.buildThing(URI.create("http://www.example.com/ROs/ro/.ro/evo_info.ttlordinary")),
            new HashSet<Thing>(Arrays.asList(ro)));

        Annotation ordinaryAnnotation3 = builder.buildAnnotation(ro,
            URI.create("http://www.example.com/ROs/ro/annotations/evo_info.ttl/annotation"),
            builder.buildThing(URI.create("http://www.example.com/ROs/ro/annotations/evo_info.ttl/annotation")),
            new HashSet<Thing>(Arrays.asList(ro)));

        Annotation specialAnnotation1 = builder.buildAnnotation(ro,
            URI.create("http://www.example.com/ROs/ro/annotations/annotation"),
            builder.buildThing(URI.create("http://www.example.com/ROs/ro/.ro/manifest.rdf")),
            new HashSet<Thing>(Arrays.asList(ro)));

        Annotation specialAnnotation2 = builder.buildAnnotation(ro,
            URI.create("http://www.example.com/ROs/ro/annotations/annotation"),
            builder.buildThing(URI.create("http://www.example.com/ROs/ro/.ro/evo_info.ttl")),
            new HashSet<Thing>(Arrays.asList(ro)));

        Annotation specialAnnotation3 = builder.buildAnnotation(ro, URI
                .create("http://www.example.com/ROs/ro/annotations/annotation"), builder.buildThing(URI
                .create("http://www.example.com/ROs/ro/.ro/evo_info.ttl/")), new HashSet<Thing>(Arrays.asList(ro)));

        Assert.assertFalse(ordinaryAnnotation1.isSpecialResource());
        Assert.assertFalse(ordinaryAnnotation2.isSpecialResource());
        Assert.assertFalse(ordinaryAnnotation3.isSpecialResource());
        Assert.assertTrue(specialAnnotation1.isSpecialResource());
        Assert.assertTrue(specialAnnotation2.isSpecialResource());
        Assert.assertTrue(specialAnnotation3.isSpecialResource());
    }
}
