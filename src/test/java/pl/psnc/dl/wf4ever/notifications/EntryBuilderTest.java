package pl.psnc.dl.wf4ever.notifications;

import java.net.URI;

import junit.framework.Assert;

import org.junit.Test;

import pl.psnc.dl.wf4ever.db.AtomFeedEntry;
import pl.psnc.dl.wf4ever.model.BaseTest;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

public class EntryBuilderTest extends BaseTest {

    URI researchObjectUri = URI.create("http://www.example.org/ROs/research-object/");
    ResearchObject researchObject = new ResearchObject(userProfile, dataset, true, researchObjectUri);


    @Test
    public void testCreateOnCreateEvent() {

        AtomFeedEntry entry = EntryBuilder.create(researchObject, ActionType.NEW_RO);
        Assert.assertNotNull(entry.getCreated());
        Assert.assertNotNull(entry.getId());
        Assert.assertTrue(entry.getSubject().equals(researchObjectUri));
        Assert.assertTrue(entry.getTitle().contains("Research Object has been created"));
        Assert.assertTrue(entry.getSummary().contains("Research Object has been created"));
    }


    @Test
    public void testCreateOnDeleteEvent() {
        AtomFeedEntry entry = EntryBuilder.create(researchObject, ActionType.DELETED_RO);
        Assert.assertNotNull(entry.getCreated());
        Assert.assertNotNull(entry.getId());
        Assert.assertTrue(entry.getSubject().equals(researchObjectUri));
        Assert.assertTrue(entry.getTitle().contains("Research Object has been deleted"));
        Assert.assertTrue(entry.getSummary().contains("Research Object has been deleted"));
    }

}
