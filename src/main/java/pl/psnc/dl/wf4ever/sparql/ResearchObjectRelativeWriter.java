package pl.psnc.dl.wf4ever.sparql;

import java.net.URI;

import com.hp.hpl.jena.rdf.model.RDFWriter;

public interface ResearchObjectRelativeWriter extends RDFWriter {

    public abstract void setResearchObjectURI(URI researchObjectURI);


    /**
     * @return the researchObjectURI
     */
    public abstract URI getResearchObjectURI();


    public abstract void setBaseURI(URI baseURI);
}
