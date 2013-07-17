package pl.psnc.dl.wf4ever.eventbus.events;

import java.net.URI;

import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.model.RDF.Thing;

/**
 * An event signifying that a resource should have its serialization on the disk refreshed based on the triplestore.
 * 
 * @author piotrekhol
 * 
 */
public class ScheduleToSerializationEvent {

    /** Resource to serialize. */
    private final Thing thing;

    /** The object whose URI is the base. */
    private final URI base;

    /** The format in which the resource should be saved. */
    private final RDFFormat format;


    /**
     * Constructor.
     * 
     * @param thing
     *            resource to serialize
     * @param base
     *            the object whose URI is the base
     * @param format
     *            the format in which the resource should be saved
     */
    public ScheduleToSerializationEvent(Thing thing, URI base, RDFFormat format) {
        this.thing = thing;
        this.base = base;
        this.format = format;
    }


    public Thing getThing() {
        return thing;
    }


    public URI getBase() {
        return base;
    }


    public RDFFormat getFormat() {
        return format;
    }

}
