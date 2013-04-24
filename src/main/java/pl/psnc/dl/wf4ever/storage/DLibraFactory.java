/*
 * Copyright (c) 2011 Poznan Supercomputing and Networking Center
 * 10 Noskowskiego Street, Poznan, Wielkopolska 61-704, Poland
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Poznan Supercomputing and Networking Center ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into
 * with PSNC.
 */
package pl.psnc.dl.wf4ever.storage;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.dl.DigitalLibrary;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.helpers.DLibraDataSource;

/**
 * A factory class that creates a dLibra connection class.
 * 
 * @author Piotr Hołubowicz
 * 
 */
public class DLibraFactory implements DigitalLibraryFactory {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(DLibraFactory.class);

    /** port. */
    private int port;

    /** host. */
    private String host;

    /** dLibra directory for RODL. */
    private long workspacesDirectory;

    /** collection in which to publish ROs. */
    private long collectionId;

    /** dLibra admin. */
    private String adminUser;

    /** dLibra admin password. */
    private String adminPassword;

    /** Thread local dLibra instance. */
    private final ThreadLocal<DLibraDataSource> dlibraThreadLocal = new ThreadLocal<>();


    /**
     * Constructor.
     * 
     * @param properties
     *            a properties file to load any necessary properties
     */
    public DLibraFactory(Properties properties) {
        host = properties.getProperty("host");
        port = Integer.parseInt(properties.getProperty("port"));
        LOGGER.debug("Connection parameters: " + host + ":" + port);
        workspacesDirectory = Long.parseLong(properties.getProperty("workspacesDir"));
        LOGGER.debug("Workspaces directory: " + workspacesDirectory);
        collectionId = Long.parseLong(properties.getProperty("collectionId"));
        LOGGER.debug("Collection id: " + collectionId);
        adminUser = properties.getProperty("adminUser");
        adminPassword = properties.getProperty("adminPassword");
    }


    /**
     * Get the digital library for this thread or create a new connection if not set.
     * 
     * @return a digital library instance
     */
    @Override
    public DigitalLibrary getDigitalLibrary() {
        if (dlibraThreadLocal.get() == null) {
            try {
                LOGGER.debug("Creating a dLibra backend");
                dlibraThreadLocal.set(new DLibraDataSource(host, port, workspacesDirectory, collectionId, adminUser,
                        adminPassword));
            } catch (DigitalLibraryException | IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return dlibraThreadLocal.get();
    }

}
