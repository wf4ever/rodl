package pl.psnc.dl.wf4ever.sms;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.common.ResearchObject;

public class RDFGenerator extends SemanticMetadataServiceBaseTest {

    //not a real test.
    //@Test
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
