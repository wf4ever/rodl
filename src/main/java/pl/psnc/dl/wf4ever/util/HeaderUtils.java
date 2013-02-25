package pl.psnc.dl.wf4ever.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Utility methods.
 * 
 * @author piotrekhol
 * 
 */
public final class HeaderUtils {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(HeaderUtils.class);


    /**
     * Constructor.
     */
    private HeaderUtils() {
        //nope
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
    private static List<String> split(String text, char sep, char lq, char rq) {
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
    public static Multimap<String, URI> getLinkHeaders(Collection<String> values) {
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
}
