package pl.psnc.dl.wf4ever.notifications;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

public class AtomFeedEntryTest {

    @Test
    public void testConstructor() {
        Notification entry = new Notification();
        //check the date transfer
        DateTime now = DateTime.now();
        entry.setCreated(now.toDate());
        DateTime trasfered = new DateTime(entry.getCreated());
        Assert.assertEquals(now, trasfered);
    }
}