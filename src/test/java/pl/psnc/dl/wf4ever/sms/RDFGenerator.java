package pl.psnc.dl.wf4ever.sms;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.common.ResearchObject;

/***
 * @author pejot Created to translate ttl format of test example to rdf.
 */
public class RDFGenerator extends SemanticMetadataServiceBaseTest {

    /**
     * As a JUnit test generates manifest file in rdf format from one of example.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    public final void generateRDF()
            throws URISyntaxException, IOException {
        URI fakeURI = new URI("http://www.example.com/ROs/");
        InputStream is = getClass().getClassLoader().getResourceAsStream(
            "/src/test/resources/rdfStructure/ro1/.ro/manifest.ttl");
        SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, ResearchObject.create(fakeURI), is,
                RDFFormat.TURTLE);
        try {
            ResearchObject researchObject = ResearchObject.create(fakeURI);
            System.out.println(IOUtils.toString(sms.getNamedGraphWithRelativeURIs(researchObject.getManifestUri(),
                researchObject, RDFFormat.RDFXML)));
        } finally {
            sms.close();
        }
    }

}
