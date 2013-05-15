package pl.psnc.dl.wf4ever.preservation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.IntegrationTest;
import pl.psnc.dl.wf4ever.W4ETest;
import pl.psnc.dl.wf4ever.darceo.client.DArceoClient;
import pl.psnc.dl.wf4ever.darceo.client.DArceoException;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectComponentSerializable;
import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectSerializable;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.sun.jersey.api.client.ClientResponse;

@Category(IntegrationTest.class)
public class PreservationTest extends W4ETest {

    int SINGLE_BREAK = 10;
    int MAX_PAUSES_NUMBER = 50;


    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        createUserWithAnswer(userIdSafe, username).close();
        accessToken = createAccessToken(userId);
    }


    @Override
    public void tearDown()
            throws Exception {
        deleteROs();
        deleteAccessToken(accessToken);
        deleteUser(userIdSafe);
        super.tearDown();
    }


    @Test
    public void testCreateAndPresarve()
            throws DArceoException, IOException, InterruptedException {
        URI cretedRO = createRO(accessToken);
        ResearchObjectSerializable returnedRO = DArceoClient.getInstance().getBlocking(cretedRO);
        int counter = 0;
        while (returnedRO == null) {
            Thread.sleep(100);
            returnedRO = DArceoClient.getInstance().getBlocking(cretedRO);
            if (counter++ >= MAX_PAUSES_NUMBER) {
                assert false;
            }
        }
        Assert.assertEquals(cretedRO, returnedRO.getUri());
    }


    @Test
    public void testUpdateAndPreserver()
            throws DArceoException, IOException, InterruptedException {
        String filePath = "added/file";
        URI cretedRO = createRO(accessToken);
        ClientResponse response = addFile(cretedRO, filePath, accessToken);
        int counter = 0;
        while (true) {
            ResearchObjectSerializable returnedRO = DArceoClient.getInstance().getBlocking(cretedRO);
            if (returnedRO.getSerializables().containsKey(returnedRO.getUri().resolve(filePath))) {
                break;
            }
            if (counter++ >= MAX_PAUSES_NUMBER) {
                assert false;
            }
        }
    }


    @Test
    public void testAnnotateAndPreserve()
            throws InterruptedException, DArceoException, IOException {
        String annotationBodyPath = "annotation/body";
        URI cretedRO = createRO(accessToken);
        InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationGood.rdf");
        ClientResponse response = addAnnotation(is, cretedRO, annotationBodyPath, accessToken);
        IOUtils.closeQuietly(is);
        ResearchObjectSerializable returnedRO;
        int counter = 0;
        while (true) {
            returnedRO = DArceoClient.getInstance().getBlocking(cretedRO);
            if (returnedRO.getSerializables().containsKey(returnedRO.getUri().resolve(annotationBodyPath))) {
                break;
            }
            if (counter++ >= MAX_PAUSES_NUMBER) {
                assert false;
            }
        }
        is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationGood.rdf");
        InputStream isA = returnedRO.getSerializables().get(returnedRO.getUri().resolve(annotationBodyPath))
                .getSerialization();
        Model m = ModelFactory.createDefaultModel();
        m.read(is, "");
        Model mA = ModelFactory.createDefaultModel();
        mA.read(isA, "");
        Assert.assertTrue(m.isIsomorphicWith(mA));
    }


    @Test
    public void testUpdateFileAndPReserve()
            throws DArceoException, IOException, InterruptedException {
        String filePath = "added/file";
        URI cretedRO = createRO(accessToken);
        ClientResponse response = addFile(cretedRO, filePath, accessToken);
        response = webResource.uri(cretedRO).path(filePath).header("Authorization", "Bearer " + accessToken)
                .type("text/plain").put(ClientResponse.class, "lorem ipsum");
        response = webResource.uri(cretedRO).path(filePath).header("Authorization", "Bearer " + accessToken)
                .type("text/plain").put(ClientResponse.class, "new content");
        int counter = 0;
        Thread.sleep(10000);
        while (true) {
            ResearchObjectSerializable returnedRO = DArceoClient.getInstance().getBlocking(cretedRO);
            //here is a test.
            if (returnedRO.getSerializables().containsKey(returnedRO.getUri().resolve(filePath))) {
                ResearchObjectComponentSerializable component = returnedRO.getSerializables().get(
                    returnedRO.getUri().resolve(filePath));
                if (IOUtils.toString(component.getSerialization()).equals("new content")) {
                    break;
                }

            }
            if (counter++ >= MAX_PAUSES_NUMBER) {
                assert false;
            }

        }
    }
}
