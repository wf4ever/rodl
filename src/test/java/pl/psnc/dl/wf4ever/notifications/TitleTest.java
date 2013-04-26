package pl.psnc.dl.wf4ever.notifications;

import java.net.URI;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import pl.psnc.dl.wf4ever.notifications.NotificationFeed.Title;

/**
 * Test of a helper class for building feed titles.
 * 
 * @author piotrekhol
 * 
 */
public class TitleTest {

    /** Sample RO URI. */
    private URI researchObjectUri = URI.create("http://www.example.com/ROs/research-object/");


    /**
     * Test building title with no params.
     */
    @Test
    public void testBuldTitleNoParams() {
        String title = Title.build(null, null, null);
        Assert.assertTrue(title.contains("ROs"));
        Assert.assertFalse(title.contains("Range"));
        Assert.assertFalse(title.contains("from"));
        Assert.assertFalse(title.contains("to"));
    }


    /**
     * Test building title with only range.
     */
    @Test
    public void testBuldTitleNoRO() {
        String title = Title.build(null, DateTime.now().toDate(), null);
        Assert.assertTrue(title.contains("ROs"));
        Assert.assertTrue(title.contains("Range"));
        Assert.assertTrue(title.contains("from"));
        Assert.assertFalse(title.contains("to"));

        title = Title.build(null, DateTime.now().toDate(), DateTime.now().toDate());
        Assert.assertTrue(title.contains("ROs"));
        Assert.assertTrue(title.contains("Range"));
        Assert.assertTrue(title.contains("from"));
        Assert.assertTrue(title.contains("to"));

        title = Title.build(null, null, DateTime.now().toDate());
        Assert.assertTrue(title.contains("ROs"));
        Assert.assertTrue(title.contains("Range"));
        Assert.assertFalse(title.contains("from"));
        Assert.assertTrue(title.contains("to"));
    }


    /**
     * Test building title with all params.
     */
    @Test
    public void testBuldTitleRO() {
        String title = Title.build(researchObjectUri, null, null);
        Assert.assertTrue(title.contains(researchObjectUri.toString()));
        Assert.assertFalse(title.contains("Range"));
        Assert.assertFalse(title.contains("from"));
        Assert.assertFalse(title.contains("to"));

        title = Title.build(researchObjectUri, DateTime.now().toDate(), DateTime.now().toDate());
        Assert.assertTrue(title.contains(researchObjectUri.toString()));
        Assert.assertTrue(title.contains("Range"));
        Assert.assertTrue(title.contains("from"));
        Assert.assertTrue(title.contains("to"));

        title = Title.build(researchObjectUri, null, DateTime.now().toDate());
        Assert.assertTrue(title.contains(researchObjectUri.toString()));
        Assert.assertTrue(title.contains("Range"));
        Assert.assertFalse(title.contains("from"));
        Assert.assertTrue(title.contains("to"));
    }
}
