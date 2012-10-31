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

import pl.psnc.dl.wf4ever.common.UserProfile;
import pl.psnc.dl.wf4ever.dl.DigitalLibrary;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.helpers.DLibraDataSource;
import pl.psnc.dl.wf4ever.fs.FilesystemDL;

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

    /** use dlibra? */
    private static boolean dlibra;

    /** the root folder for filesystem storage. */
    private static String filesystemBase;

    /** admin's access token. */
    private static String adminToken;

    /** dLibra admin. */
    private static String adminUser;

    /** dLibra admin password. */
    private static String adminPassword;

    /** dLibra public user. */
    private static String publicUser;

    /** dLibra public user password. */
    private static String publicPassword;

    /** the user that theoretically is logged in to dLibra, a hack. */
    private static String dLibraUser;


    /**
     * Private constructor.
     */
    private DigitalLibraryFactory() {
        //nope
    }


    /**
     * Create a new connection to the digital library.
     * 
     * @param userId
     *            user login in digital library
     * @return a digital library connection
     * @throws RemoteException
     *             no connection to dLibra
     * @throws MalformedURLException
     *             the host is incorrect
     * @throws UnknownHostException
     *             the host does not respond
     */
    public static DigitalLibrary getDigitalLibrary(String userId)
            throws RemoteException, MalformedURLException, UnknownHostException {
        if (dlibra) {
            try {
                LOGGER.debug("Creating a dLibra backend");
                dLibraUser = userId;
                return new DLibraDataSource(host, port, workspacesDirectory, collectionId, adminUser, adminPassword);
            } catch (DigitalLibraryException | IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        } else {
            LOGGER.debug("Creating a filesystem backend");
            UserProfile user;
            if (userId.equals(DigitalLibraryFactory.getAdminUser())) {
                user = UserProfile.ADMIN;
            } else if (userId.equals(DigitalLibraryFactory.getPublicUser())) {
                user = UserProfile.PUBLIC;
            } else {
                user = UserProfile.findByLogin(userId);
            }
            return new FilesystemDL(filesystemBase, user);
        }
    }


    /**
     * Get profile of the user logged in the digital library.
     * 
     * This is bad design and should be fixed be removing the user credentials class (the password is never used!) and
     * using the user profile instead.
     * 
     * @param dl
     *            digital library
     * @return user profile
     * @throws DigitalLibraryException
     *             dl error
     * @throws NotFoundException
     *             user not found
     */
    public static UserProfile getUserProfile(DigitalLibrary dl)
            throws DigitalLibraryException, NotFoundException {
        if (dl instanceof DLibraDataSource) {
            return dl.getUserProfile(dLibraUser);
        } else if (dl instanceof FilesystemDL) {
            return dl.getUserProfile();
        }
        return null;
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
        dlibra = "true".equals(properties.getProperty("dlibra", "false"));
        LOGGER.debug("Use dlibra: " + dlibra);

        host = properties.getProperty("host");
        port = Integer.parseInt(properties.getProperty("port"));
        LOGGER.debug("Connection parameters: " + host + ":" + port);

        workspacesDirectory = Long.parseLong(properties.getProperty("workspacesDir"));
        LOGGER.debug("Workspaces directory: " + workspacesDirectory);

        collectionId = Long.parseLong(properties.getProperty("collectionId"));
        LOGGER.debug("Collection id: " + collectionId);

        filesystemBase = properties.getProperty("filesystemBase", "/tmp/dl/");
        LOGGER.debug("Filesystem base: " + filesystemBase);
    }


    /**
     * Load connection details.
     * 
     * @param configFileName
     *            properties file with the connection details
     */
    public static void loadProfilesConfiguration(String configFileName) {
        LOGGER.info("Loading profiles file " + configFileName);
        InputStream inputStream = DigitalLibraryFactory.class.getClassLoader().getResourceAsStream(configFileName);
        if (inputStream == null) {
            LOGGER.error("Profiles file not found! ");
            throw new RuntimeException("Profiles file not found! ");
        }
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            LOGGER.error("Unable to read profiles file", e);
            throw new RuntimeException("Unable to read profiles file", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                LOGGER.warn("Exception when closing the properties input stream", e);
            }
        }
        adminToken = properties.getProperty("adminToken");
        adminUser = properties.getProperty("adminUser");
        adminPassword = properties.getProperty("adminPassword");
        publicUser = properties.getProperty("publicUser");
        publicPassword = properties.getProperty("publicPassword");
    }


    public static String getAdminToken() {
        return adminToken;
    }


    public static String getAdminUser() {
        return adminUser;
    }


    public static String getAdminPassword() {
        return adminPassword;
    }


    public static String getPublicUser() {
        return publicUser;
    }


    public static String getPublicPassword() {
        return publicPassword;
    }
}
