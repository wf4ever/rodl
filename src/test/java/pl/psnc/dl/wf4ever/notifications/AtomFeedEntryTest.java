package pl.psnc.dl.wf4ever.notifications;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Test;

import pl.psnc.dl.wf4ever.notifications.AtomFeedEntry;

public class AtomFeedEntryTest {

    @Test
    public void testConstructor() {
        AtomFeedEntry entry = new AtomFeedEntry();
        //check the date transfer
        DateTime now = DateTime.now();
        entry.setCreated(now.toDate());
        DateTime trasfered = new DateTime(entry.getCreated());
        Assert.assertEquals(now, trasfered);
    }
}
