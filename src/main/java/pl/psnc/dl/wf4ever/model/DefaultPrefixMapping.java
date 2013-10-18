package pl.psnc.dl.wf4ever.model;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Utility class that creates a prefix mapping with commonly used prefixes.
 * 
 * @author piotrekhol
 * 
 */
public final class DefaultPrefixMapping {

    /** the only instance. */
    private static PrefixMapping instance = null;


    /**
     * Private constructor.
     */
    private DefaultPrefixMapping() {
        //nope
    }


    /**
     * Get the prefix mapping.
     * 
     * @return a locked prefix mapping
     */
    public static PrefixMapping get() {
        if (instance == null) {
            instance = createInstance();
        }
        return instance;
    }


    /**
     * Create the prefix mapping.
     * 
     * @return a locked prefix mapping
     */
    private static PrefixMapping createInstance() {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("ro", "http://purl.org/wf4ever/ro#");
        model.setNsPrefix("roevo", "http://purl.org/wf4ever/roevo#");
        model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        model.setNsPrefix("ore-owl", "http://purl.org/wf4ever/ore-owl");
        model.setNsPrefix("ore", "http://www.openarchives.org/ore/terms/");
        model.setNsPrefix("ao", "http://purl.org/ao/");
        model.setNsPrefix("aocore", "http://purl.org/ao/core/");
        model.setNsPrefix("skos", "http://www.w3.org/TR/skos-reference/skos-owl1-dl.rdf");
        model.setNsPrefix("pav", "http://purl.org/pav/2.0/");
        model.setNsPrefix("prov", "http://www.w3.org/ns/prov#");
        model.setNsPrefix("pavauth", "http://purl.org/pav/authoring/2.0/");
        model.setNsPrefix("pavprov", "http://purl.org/pav/provenance/2.0/");
        model.setNsPrefix("wfdesc", "http://purl.org/wf4ever/wfdesc#");
        model.setNsPrefix("wfprov", "http://purl.org/wf4ever/wfprov#");
        model.setNsPrefix("wf4ever", "http://purl.org/wf4ever/wf4ever#");
        model.setNsPrefix("roterms", "http://purl.org/wf4ever/roterms#");
        model.setNsPrefix("dct", "http://purl.org/dc/terms/");
        model.lock();
        return model;
    }
}
