package pl.psnc.dl.wf4ever.hibernate;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;

import pl.psnc.dl.wf4ever.common.HibernateUtil;

/**
 * Copied from https://community.jboss.org/wiki/OpenSessionInView.
 * 
 * @author piotrek
 * 
 */
public class HibernateSessionRequestFilter implements Filter {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(HibernateSessionRequestFilter.class);

    /** session factory. */
    private SessionFactory sf;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            LOGGER.debug("Starting a database transaction");
            sf.getCurrentSession().beginTransaction();

            // Call the next filter (continue request processing)
            chain.doFilter(request, response);

            // Commit and cleanup
            LOGGER.debug("Committing the database transaction");
            sf.getCurrentSession().getTransaction().commit();

        } catch (StaleObjectStateException staleEx) {
            LOGGER.error("This interceptor does not implement optimistic concurrency control!");
            LOGGER.error("Your application will not work until you add compensation actions!");
            // Rollback, close everything, possibly compensate for any permanent changes
            // during the conversation, and finally restart business conversation. Maybe
            // give the user of the application a chance to merge some of his work with
            // fresh data... what you do here depends on your applications design.
            throw staleEx;
        } catch (Throwable ex) {
            // Rollback only
            ex.printStackTrace();
            try {
                if (sf.getCurrentSession().getTransaction().isActive()) {
                    LOGGER.debug("Trying to rollback database transaction after exception");
                    sf.getCurrentSession().getTransaction().rollback();
                }
            } catch (Throwable rbEx) {
                LOGGER.error("Could not rollback transaction after exception!", rbEx);
            }

            // Let others handle it... maybe another interceptor for exceptions?
            throw new ServletException(ex);
        }
    }


    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException {
        LOGGER.debug("Initializing filter...");
        LOGGER.debug("Obtaining SessionFactory from static HibernateUtil singleton");
        sf = HibernateUtil.getSessionFactory();
    }


    @Override
    public void destroy() {
    }

}