package pl.psnc.dl.wf4ever.notifications;

import java.net.URI;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Test;

public class AtomFeedTitleBuilderTest {

    private URI reserachObjectUri = URI.create("http://www.example.com/ROs/research-object/");


    @Test
    public void testBuldTitleNoParams() {
        String title = AtomFeedTitileBuilder.buildTitle(null, null, null);
        Assert.assertTrue(title.contains("ROs"));
        Assert.assertFalse(title.contains("Range"));
        Assert.assertFalse(title.contains("from"));
        Assert.assertFalse(title.contains("to"));
    }


    @Test
    public void testBuldTitleNoRO() {
        String title = AtomFeedTitileBuilder.buildTitle(null, DateTime.now().toDate(), null);
        Assert.assertTrue(title.contains("ROs"));
        Assert.assertTrue(title.contains("Range"));
        Assert.assertTrue(title.contains("from"));
        Assert.assertFalse(title.contains("to"));

        title = AtomFeedTitileBuilder.buildTitle(null, DateTime.now().toDate(), DateTime.now().toDate());
        Assert.assertTrue(title.contains("ROs"));
        Assert.assertTrue(title.contains("Range"));
        Assert.assertTrue(title.contains("from"));
        Assert.assertTrue(title.contains("to"));

        title = AtomFeedTitileBuilder.buildTitle(null, null, DateTime.now().toDate());
        Assert.assertTrue(title.contains("ROs"));
        Assert.assertTrue(title.contains("Range"));
        Assert.assertFalse(title.contains("from"));
        Assert.assertTrue(title.contains("to"));
    }


    @Test
    public void testBuldTitleRO() {
        String title = AtomFeedTitileBuilder.buildTitle(reserachObjectUri, null, null);
        Assert.assertTrue(title.contains(reserachObjectUri.toString()));
        Assert.assertFalse(title.contains("Range"));
        Assert.assertFalse(title.contains("from"));
        Assert.assertFalse(title.contains("to"));

        title = AtomFeedTitileBuilder.buildTitle(reserachObjectUri, DateTime.now().toDate(), DateTime.now().toDate());
        Assert.assertTrue(title.contains(reserachObjectUri.toString()));
        Assert.assertTrue(title.contains("Range"));
        Assert.assertTrue(title.contains("from"));
        Assert.assertTrue(title.contains("to"));

        title = AtomFeedTitileBuilder.buildTitle(reserachObjectUri, null, DateTime.now().toDate());
        Assert.assertTrue(title.contains(reserachObjectUri.toString()));
        Assert.assertTrue(title.contains("Range"));
        Assert.assertFalse(title.contains("from"));
        Assert.assertTrue(title.contains("to"));
    }
}
