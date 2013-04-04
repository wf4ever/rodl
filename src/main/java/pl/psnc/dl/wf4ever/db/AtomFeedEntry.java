package pl.psnc.dl.wf4ever.db;

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
    private Date created = DateTime.now().toDate();

    /** Title. */
    private String title;

    /** The event source/discover. */
    private String source;

    /** Summary/Description(Content). */
    private String summary;

    /** Related object (for example Research Object uri). */
    @Column(nullable = false)
    private String subject;


    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }


    public URI getSource() {
        return URI.create(source);
    }


    public void setSource(URI source) {
        this.source = source.toString();
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


    public URI getSubject() {
        return URI.create(subject);
    }


    public void setSubject(URI subject) {
        this.subject = subject.toString();
    }

}
