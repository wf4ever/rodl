package pl.psnc.dl.wf4ever.common;

import java.net.URI;

import junit.framework.Assert;

import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.model.RO.Folder;

/**
 * ResearchObject unit tests.
 * 
 * @author piotrek
 * 
 */
public class FolderTest {

    /**
     * Test correct resource map URI calculation.
     */
    @Test
    public void testGetResourceMapUri() {
        Folder f = new Folder(null, null, URI.create("http://example.com"), null, null, null, null, false);
        Assert.assertEquals(URI.create("http://example.com/folder.rdf"), f.getResourceMapUri());
        f.setUri(URI.create("http://example.com/"));
        Assert.assertEquals(URI.create("http://example.com/folder.rdf"), f.getResourceMapUri());
        f.setUri(URI.create("http://example.com/foobar"));
        Assert.assertEquals(URI.create("http://example.com/foobar.rdf"), f.getResourceMapUri());
        f.setUri(URI.create("http://example.com/foobar/"));
        Assert.assertEquals(URI.create("http://example.com/foobar/foobar.rdf"), f.getResourceMapUri());
        f.setUri(URI.create("http://example.com/foo%20bar/"));
        Assert.assertEquals(URI.create("http://example.com/foo%20bar/foo%20bar.rdf"), f.getResourceMapUri());
        f.setUri(URI.create("http://example.com/foobar/"));
        Assert.assertEquals(URI.create("http://example.com/foobar/foobar.ttl?original=foobar.rdf"),
            f.getResourceMapUri(RDFFormat.TURTLE));
    }
}
