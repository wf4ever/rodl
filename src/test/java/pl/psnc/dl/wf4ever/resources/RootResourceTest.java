package pl.psnc.dl.wf4ever.resources;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.psnc.dl.wf4ever.IntegrationTest;
import pl.psnc.dl.wf4ever.W4ETest;
import pl.psnc.dl.wf4ever.vocabulary.NotificationService;
import pl.psnc.dl.wf4ever.vocabulary.ROEVOService;

import com.damnhandy.uri.template.UriTemplate;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

@Category(IntegrationTest.class)
public class RootResourceTest extends W4ETest {

    @Test
    public void testNotificationServiceDescription() {
        Model model = ModelFactory.createDefaultModel();
        model.read(resource().getURI().toString());
        RDFNode val = model.getProperty(model.createResource(resource().getURI().toString()),
            NotificationService.notifications).getObject();
        UriTemplate uriTemplate = UriTemplate.fromTemplate(val.toString());
        uriTemplate.set("ro", "ro-value");
        uriTemplate.set("from", "from-value");
        uriTemplate.set("to", "to-value");
        uriTemplate.set("source", "source-value");
        uriTemplate.set("limit", "limit-value");
        Assert.assertTrue(uriTemplate.getValues().containsKey("ro"));
        Assert.assertTrue(uriTemplate.getValues().containsKey("from"));
        Assert.assertTrue(uriTemplate.getValues().containsKey("to"));
        Assert.assertTrue(uriTemplate.getValues().containsKey("source"));
        Assert.assertTrue(uriTemplate.getValues().containsKey("limit"));
        Assert.assertFalse(uriTemplate.getValues().containsKey("any-other"));
    }


    @Test
    public void testEvolutionServiceDescription() {
        Model model = ModelFactory.createDefaultModel();
        model.read(resource().getURI().toString());
        RDFNode val = model.getProperty(model.createResource(resource().getURI().toString()), ROEVOService.info)
                .getObject();
        UriTemplate uriTemplate = UriTemplate.fromTemplate(val.toString());
        uriTemplate.set("ro", "ro-value");

        Assert.assertFalse(uriTemplate.getValues().containsKey("any-other"));
    }
}
