package pl.psnc.dl.wf4ever.model.ORE;

import java.net.URI;

import pl.psnc.dl.wf4ever.model.RDF.Thing;

/**
 * Represents an ore:Proxy.
 * 
 * @author piotrekhol
 * 
 */
public class Proxy extends Thing {

    /** URI of aggregated resource. */
    protected URI proxyFor;

    /** URI of aggregating resource. */
    protected URI proxyIn;


    public URI getProxyIn() {
        return proxyIn;
    }


    public void setProxyIn(URI proxyIn) {
        this.proxyIn = proxyIn;
    }


    public URI getProxyFor() {
        return proxyFor;
    }


    public void setProxyFor(URI proxyFor) {
        this.proxyFor = proxyFor;
    }

}
