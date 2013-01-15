package pl.psnc.dl.wf4ever.common;

import java.net.URI;

import junit.framework.Assert;

import org.junit.Test;

import pl.psnc.dl.wf4ever.model.RO.Folder;
import pl.psnc.dl.wf4ever.model.RO.FolderResourceMap;

/**
 * ResearchObject unit tests.
 * 
 * @author piotrek
 * 
 */
public class FolderResourceMapTest {

    /**
     * Test correct resource map URI calculation.
     */
    @Test
    public void testGetResourceMapUri() {
        Folder f = new Folder(null, null, URI.create("http://example.com"));
        Assert.assertEquals(URI.create("http://example.com/folder.rdf"), FolderResourceMap.generateResourceMapUri(f));
        f.setUri(URI.create("http://example.com/"));
        Assert.assertEquals(URI.create("http://example.com/folder.rdf"), FolderResourceMap.generateResourceMapUri(f));
        f.setUri(URI.create("http://example.com/foobar"));
        Assert.assertEquals(URI.create("http://example.com/foobar.rdf"), FolderResourceMap.generateResourceMapUri(f));
        f.setUri(URI.create("http://example.com/foobar/"));
        Assert.assertEquals(URI.create("http://example.com/foobar/foobar.rdf"),
            FolderResourceMap.generateResourceMapUri(f));
        f.setUri(URI.create("http://example.com/foo%20bar/"));
        Assert.assertEquals(URI.create("http://example.com/foo%20bar/foo%20bar.rdf"),
            FolderResourceMap.generateResourceMapUri(f));
        f.setUri(URI.create("http://example.com/foobar/"));
    }

}
