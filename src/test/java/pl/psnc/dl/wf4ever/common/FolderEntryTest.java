package pl.psnc.dl.wf4ever.common;

import java.net.URI;

import junit.framework.Assert;

import org.junit.Test;

import pl.psnc.dl.wf4ever.model.RO.FolderEntry;

/**
 * ResearchObject unit tests.
 * 
 * @author piotrek
 * 
 */
public class FolderEntryTest {

    /**
     * Test correct initial values.
     */
    @Test
    public void testGenerateEntryname() {
        Assert.assertEquals("foo", FolderEntry.generateEntryName(URI.create("http://example.org/foo")));
        Assert.assertEquals("foo/", FolderEntry.generateEntryName(URI.create("http://example.org/foo/")));
        Assert.assertEquals("http://example.org/", FolderEntry.generateEntryName(URI.create("http://example.org/")));
        Assert.assertEquals("http://example.org", FolderEntry.generateEntryName(URI.create("http://example.org")));
    }

}
