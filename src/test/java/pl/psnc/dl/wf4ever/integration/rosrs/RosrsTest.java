package pl.psnc.dl.wf4ever.integration.rosrs;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.integration.AbstractIntegrationTest;
import pl.psnc.dl.wf4ever.integration.IntegrationTest;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

@Category(IntegrationTest.class)
public class RosrsTest extends AbstractIntegrationTest {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(RosrsTest.class);

    /** Sample RO created before every test. */
    protected URI ro;

    /** Sample RO created before every test. */
    protected URI ro2;


    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        ro = createRO();
        ro2 = createRO();
    }


    /**
     * Helper function returns list of delimited values in a string, where delimiters in quotes are protected.
     * 
     * @param text
     *            text to search
     * @param sep
     *            separator
     * @param lq
     *            left quote
     * @param rq
     *            right quote
     * @return list of values
     */
    private List<String> split(String text, char sep, char lq, char rq) {
        List<String> result = new ArrayList<>();
        int cursor = 0;
        int begseg = cursor;
        while (cursor < text.length()) {
            if (text.charAt(cursor) == lq) {
                // Skip quoted or bracketed string
                char eq = rq; // End quote/bracket character
                cursor++;
                while (cursor < text.length() && text.charAt(cursor) != eq) {
                    if (text.substring(cursor).startsWith("\\")) {
                        cursor++;
                        // skip '\' quoted-pair
                    }
                    cursor++;
                }
                if (cursor < text.length()) {
                    cursor++; // Skip closing quote/bracket
                }
            } else if (text.charAt(cursor) == sep) {
                result.add(text.substring(begseg, cursor));
                cursor++;
                begseg = cursor;
            } else {
                cursor++;
            }
        }
        // append final segment
        result.add(text.substring(begseg, cursor));
        return result;
    }


    /**
     * Return a link header value for a given rel.
     * 
     * Copied from RO manager ROSRS_Session.
     * 
     * @param values
     *            header values
     * @return Multimap, key is rel, values are URIs
     */
    protected Multimap<String, URI> getLinkHeaders(List<String> values) {
        Multimap<String, URI> result = HashMultimap.<String, URI> create();
        Pattern uriPattern = Pattern.compile("\\s*<([^>]*)>\\s*");
        Pattern relPattern = Pattern.compile("\\s*rel\\s*=\\s*\"?(.*?)\"?\\s*$");
        for (String value : values) {
            String[] lines = value.split(",");
            for (String line : lines) {
                List<String> parts = split(line, ';', '<', '>');
                Matcher m = uriPattern.matcher(parts.get(0));
                if (m.matches()) {
                    String link = m.group(1);
                    try {
                        URI linkUri = new URI(link);
                        for (int i = 1; i < parts.size(); i++) {
                            Matcher m2 = relPattern.matcher(parts.get(i));
                            if (m2.matches()) {
                                String linkRel = m2.group(1);
                                result.put(linkRel, linkUri);
                            }
                        }
                    } catch (URISyntaxException e) {
                        LOGGER.debug("A link header has an invalid URI", e);
                    }
                }
            }
        }
        return result;
    }


    /**
     * Test the method for parsing the link headers.
     */
    @Test
    public void testGetLinkHeaders() {
        List<String> links = Arrays.asList("<http://example.org/foo>; rel=foo",
            " <http://example.org/bar> ; rel = bar ",
            "<http://example.org/bas>; rel=bas; par = zzz , <http://example.org/bat>; rel = bat",
            " <http://example.org/fie> ; par = fie ",
            " <http://example.org/fum> ; rel = \"http://example.org/rel/fum\" ",
            " <http://example.org/fas;far> ; rel = \"http://example.org/rel/fas\" ");
        Multimap<String, URI> result = getLinkHeaders(links);
        Assert.assertTrue(result.get("foo").contains(URI.create("http://example.org/foo")));
        Assert.assertTrue(result.get("bar").contains(URI.create("http://example.org/bar")));
        Assert.assertTrue(result.get("bas").contains(URI.create("http://example.org/bas")));
        Assert.assertTrue(result.get("bat").contains(URI.create("http://example.org/bat")));
        Assert.assertTrue(result.get("http://example.org/rel/fum").contains(URI.create("http://example.org/fum")));
        Assert.assertTrue(result.get("http://example.org/rel/fas").contains(URI.create("http://example.org/fas;far")));
    }


    protected InputStream getManifest(URI ro, RDFFormat format) {
        return webResource.uri(ro).header("Authorization", "Bearer " + accessToken).accept(format.getDefaultMIMEType())
                .get(InputStream.class);
    }

}
