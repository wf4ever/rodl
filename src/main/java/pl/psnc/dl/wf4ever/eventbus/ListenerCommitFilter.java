package pl.psnc.dl.wf4ever.eventbus;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.model.Builder;

/**
 * Commit all actions reported by resources using events.
 * 
 * @author piotrek
 * 
 */
public class ListenerCommitFilter implements Filter {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(ListenerCommitFilter.class);


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Call the next filter (continue request processing)
        chain.doFilter(request, response);

        // Commit and cleanup
        LOGGER.debug("Committing the event listeners");
        Builder builder = (Builder) request.getAttribute("Builder");
        if (builder == null) {
            LOGGER.warn("Builder is null, can't commit event listeners");
        } else {
            builder.getEventBusModule().commit();
        }
        LOGGER.debug("Committed the event listeners");
    }


    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException {
    }


    @Override
    public void destroy() {
    }

}
