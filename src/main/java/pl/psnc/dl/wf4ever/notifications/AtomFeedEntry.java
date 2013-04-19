package pl.psnc.dl.wf4ever.notifications;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

/**
 * Represents a simple entry of the feed, contains a simple report of the event like update, damage and so on.
 * 
 * @author pejot
 */
@Entity
@Table(name = "atom_feed_entries")
public class AtomFeedEntry implements Serializable {

    /** Serialization. */
    private static final long serialVersionUID = 1L;
    /** Id. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Timestamp. */
    @Column(nullable = false)
    private Date created;

    /** Title. */
    private String title;

    /** The event source/discover. */
    private String source;

    /** Summary/Description(Content). */
    private String summary;

    /** Related object (for example Research Object uri). */
    @Column(nullable = false)
    private String subject;


    public AtomFeedEntry(Builder builder) {
        this.created = builder.created;
        this.title = builder.title;
        this.source = builder.source;
        this.subject = builder.subject;
        this.summary = builder.summary;
        this.title = builder.title;
    }


    /**
     * Default constructor.
     */
    public AtomFeedEntry() {
    }


    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }


    public String getSource() {
        return source;
    }


    public void setSource(String source) {
        this.source = source;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getSummary() {
        return summary;
    }


    public void setSummary(String summary) {
        this.summary = summary;
    }


    public Date getCreated() {
        return created;
    }


    public void setCreated(Date created) {
        this.created = created;
    }


    public String getSubject() {
        return subject;
    }


    public void setSubject(String subject) {
        this.subject = subject;
    }


    public static class Builder {

        /** Timestamp. */
        private Date created = DateTime.now().toDate();

        /** Title. */
        private String title;

        /** The event source/discover. */
        //FIXME use RODL URI
        private String source = ".";

        /** Summary/Description(Content). */
        private String summary;

        /** Related object (for example Research Object URI). */
        private String subject;


        public Builder(URI subject) {
            this.subject = subject.toString();
        }


        public Builder(Thing subject) {
            this(subject.getUri());
        }


        public AtomFeedEntry build() {
            return new AtomFeedEntry(this);
        }


        public Builder created(Date created) {
            this.created = created;
            return this;
        }


        public Builder created(DateTime created) {
            this.created = created.toDate();
            return this;
        }


        public Builder title(String title) {
            this.title = title;
            return this;
        }


        public Builder title(Title title) {
            this.title = title.getValue();
            return this;
        }


        public Builder source(URI source) {
            this.source = source.toString();
            return this;
        }


        public Builder summary(String summary) {
            this.summary = summary;
            return this;
        }

    }


    public enum Title {

        RESEARCH_OBJECT_CREATED("Research Object has been created"),
        RESEARCH_OBJECT_DELETED("Research Object has been deleted");

        private final String value;


        private Title(String value) {
            this.value = value;
        }


        public String getValue() {
            return value;
        }
    }


    public static class Summary {

        public static String created(ResearchObject researchObject) {
            return String
                    .format(
                        "<p>A new Research Object has been created.</p><p>The Research Object URI is <a href=\"%s\">%<s</a>.</p>",
                        researchObject.toString());
        }


        public static String deleted(ResearchObject researchObject) {
            return String.format(
                "<p>A Research Object has been deleted.</p><p>The Research Object URI was <em>%s</em>.</p>",
                researchObject.toString());
        }
    }
}
