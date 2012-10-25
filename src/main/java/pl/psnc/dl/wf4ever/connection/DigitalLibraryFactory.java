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
package pl.psnc.dl.wf4ever.connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Properties;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.auth.UserCredentials;
import pl.psnc.dl.wf4ever.dl.DigitalLibrary;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.helpers.DLibraDataSource;

/**
 * A factory class that creates a dLibra connection class.
 * 
 * @author Piotr Hołubowicz
 * 
 */
public final class DigitalLibraryFactory {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(DigitalLibraryFactory.class);

    /** port. */
    private static int port;

    /** host. */
    private static String host;

    /** dLibra directory for RODL. */
    private static long workspacesDirectory;

    /** collection in which to publish ROs. */
    private static long collectionId;


    /**
     * Private constructor.
     */
    private DigitalLibraryFactory() {
        //nope
    }


    /**
     * Create a new connection to dLibra.
     * 
     * @param creds
     *            user credentials
     * @return a digital library connection
     * @throws RemoteException
     *             no connection to dLibra
     * @throws MalformedURLException
     *             the host is incorrect
     * @throws UnknownHostException
     *             the host does not respond
     */
    public static DigitalLibrary getDigitalLibrary(UserCredentials creds)
            throws RemoteException, MalformedURLException, UnknownHostException {
        return getDigitalLibrary(creds.getUserId(), creds.getPassword());
    }


    /**
     * Create a new connection to dLibra.
     * 
     * @param userLogin
     *            user login in dLibra
     * @param password
     *            user password in dLibra
     * @return a digital library connection
     * @throws RemoteException
     *             no connection to dLibra
     * @throws MalformedURLException
     *             the host is incorrect
     * @throws UnknownHostException
     *             the host does not respond
     */
    public static DigitalLibrary getDigitalLibrary(String userLogin, String password)
            throws RemoteException, MalformedURLException, UnknownHostException {
        try {
            return new DLibraDataSource(host, port, workspacesDirectory, collectionId, userLogin, password);
        } catch (DigitalLibraryException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * Load connection details.
     * 
     * @param configFileName
     *            properties file with the connection details
     */
    public static void loadDigitalLibraryConfiguration(String configFileName) {
        LOGGER.info("Loading connection properties file " + configFileName);
        InputStream inputStream = DigitalLibraryFactory.class.getClassLoader().getResourceAsStream(configFileName);
        if (inputStream == null) {
            LOGGER.error("Connection properties file not found! ");
            throw new RuntimeException("Connection properties file not found! ");
        }
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            LOGGER.error("Unable to read connection properties", e);
            throw new RuntimeException("Unable to read connection properties", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                LOGGER.warn("Exception when closing the properties input stream", e);
            }
        }
        host = properties.getProperty("host");
        port = Integer.parseInt(properties.getProperty("port"));
        LOGGER.debug("Connection parameters: " + host + ":" + port);

        workspacesDirectory = Long.parseLong(properties.getProperty("workspacesDir"));
        LOGGER.debug("Workspaces directory: " + workspacesDirectory);

        collectionId = Long.parseLong(properties.getProperty("collectionId"));
        LOGGER.debug("Collection id: " + collectionId);
    }

}
