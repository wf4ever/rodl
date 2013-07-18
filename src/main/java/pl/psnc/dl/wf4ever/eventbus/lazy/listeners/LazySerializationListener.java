package pl.psnc.dl.wf4ever.eventbus.lazy.listeners;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.eventbus.events.ScheduleToSerializationEvent;
import pl.psnc.dl.wf4ever.model.RDF.Thing;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Listener for ResearchObject and ResearchObjectComponent, performs operation on solr indexs.
 * 
 * @author pejot
 * 
 */
public class LazySerializationListener {

    /**
     * A mutable serialization request.
     * 
     * @author piotrekhol
     * 
     */
    private static class Request {

        /** Resource to serialize. */
        private final Thing thing;

        /** The object whose URI is the base. */
        private URI base;

        /** The format in which the resource should be saved. */
        private RDFFormat format;


        /**
         * Constructor.
         * 
         * @param thing
         *            Resource to serialize
         * @param base
         *            The object whose URI is the base
         * @param format
         *            The format in which the resource should be saved
         */
        public Request(Thing thing, URI base, RDFFormat format) {
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


        public void setBase(URI base) {
            this.base = base;
        }


        public RDFFormat getFormat() {
            return format;
        }


        public void setFormat(RDFFormat format) {
            this.format = format;
        }

    }


    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(LazySerializationListener.class);

    /** Serialization requests. */
    private Map<Thing, Request> requests = new HashMap<>();


    /**
     * Constructor.
     * 
     * @param eventBus
     *            EventBus instance
     */
    public LazySerializationListener(EventBus eventBus) {
        eventBus.register(this);
    }


    /**
     * Schedule a serialization action. Merge with a previous one for the same resource, if existed.
     * 
     * @param event
     *            event with request details
     */
    @Subscribe
    public synchronized void onSchedule(ScheduleToSerializationEvent event) {
        Request previousRequest = requests.get(event.getThing());
        if (previousRequest == null) {
            requests.put(event.getThing(), new Request(event.getThing(), event.getBase(), event.getFormat()));
        } else {
            if (previousRequest.getBase() == null && event.getBase() != null) {
                previousRequest.setBase(event.getBase());
            }
            if (previousRequest.getFormat() == null && event.getFormat() != null) {
                previousRequest.setFormat(event.getFormat());
            }
        }
    }


    /**
     * Handle all requests - serialize all resources.
     */
    public synchronized void commit() {
        for (Request request : requests.values()) {
            try {
                request.getThing().serialize(request.getBase(), request.getFormat());
            } catch (Exception e) {
                LOGGER.error("Could not serialize resource " + request.getThing(), e);
            }
        }
        requests.clear();
    }
}
