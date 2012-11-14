package pl.psnc.dl.wf4ever.common;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

/**
 * Copied from http://docs.jboss.org/hibernate/orm/3.3/reference/en/html/tutorial.html.
 * 
 * @author piotrek
 * 
 */
public final class HibernateUtil {

    /** singleton. */
    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();


    /**
     * Constructor.
     */
    private HibernateUtil() {
        //nope
    }


    /**
     * Initialize Hibernate's session factory.
     * 
     * @return session factory
     */
    private static SessionFactory buildSessionFactory() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            Configuration configuration = new Configuration();
            configuration.configure();
            ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties())
                    .buildServiceRegistry();
            SessionFactory sessionFactory2 = configuration.buildSessionFactory(serviceRegistry);
            return sessionFactory2;
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }


    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

}
