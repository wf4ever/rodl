package pl.psnc.dl.wf4ever.notifications;

import java.net.URI;
import java.util.Date;

/**
 * Builder for requested Atom Feed title.
 * 
 * @author pejot
 * 
 */
public class AtomFeedTitileBuilder {

    /**
     * Hidden consturctor.
     */
    protected AtomFeedTitileBuilder() {
        //nope
    }


    /**
     * Get a Title for a requested Feed Atom.
     * 
     * @param roUri
     *            filter param - research object uri
     * @param from
     *            .toString() filter param - date from
     * @param to
     *            .toString filter param date to
     * @return a FeedAtom entity title
     */
    public static String buildTitle(URI roUri, Date from, Date to) {
        String result = "Notifications for ";
        if (roUri == null) {
            result += "all ROs";
        } else {
            result += roUri.toString();
        }
        if (from != null || to != null) {
            result += "\nRange:\n";
            if (from != null) {
                result += "\nfrom: " + from.toString();
            }
            if (to != null) {
                result += "\nto: " + to.toString();
            }
        }
        return result;
    }
}
