/**
 * 
 */
package pl.psnc.dl.wf4ever.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.vocabulary.FOAF;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * RODL user model.
 * 
 * @author piotrhol
 * 
 */
@Entity
@Table(name = "user_profiles")
public final class UserProfile extends UserMetadata implements Serializable {

    /** id. */
    private static final long serialVersionUID = -4468344863067565271L;

    /** access tokens owned by the user. */
    private List<AccessToken> tokens = new ArrayList<AccessToken>();


    /**
     * Constructor.
     */
    public UserProfile() {
        super();
    }


    /**
     * Constructor.
     * 
     * @param login
     *            login
     * @param name
     *            name
     * @param role
     *            role
     * @param uri
     *            uri
     */
    public UserProfile(String login, String name, Role role, URI uri) {
        super(login, name, role, uri);
    }


    /**
     * Constructor.
     * 
     * @param login
     *            login
     * @param name
     *            name
     * @param role
     *            role
     */
    public UserProfile(String login, String name, Role role) {
        super(login, name, role, null);
    }


    @Basic
    public URI getHomePage() {
        return super.getHomePage();
    }


    @Id
    @Column(length = 128)
    public String getLogin() {
        return super.getLogin();
    }


    @Basic
    public String getName() {
        return super.getName();
    }


    @Basic
    public Role getRole() {
        return super.getRole();
    }


    @Transient
    public URI getUri() {
        return super.getUri();
    }


    /**
     * Set URI.
     * 
     * @param uri
     *            uri as string
     */
    public void setUriString(String uri) {
        super.setUri(URI.create(uri));
    }


    @Basic
    public String getUriString() {
        return super.getUri() != null ? super.getUri().toString() : null;
    }


    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
    @XmlTransient
    public List<AccessToken> getTokens() {
        return tokens;
    }


    public void setTokens(List<AccessToken> tokens) {
        this.tokens = tokens;
    }


    public InputStream getAsInputStream(RDFFormat format) {
        OntModel userModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
        Individual agent = userModel.createIndividual(getUri().toString(), FOAF.Agent);
        userModel.add(agent, FOAF.name, getName());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        userModel.write(out, format.getName().toUpperCase());
        return new ByteArrayInputStream(out.toByteArray());
    }

}
