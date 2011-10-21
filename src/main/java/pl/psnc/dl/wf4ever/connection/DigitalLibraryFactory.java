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
import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;
import pl.psnc.dl.wf4ever.dlibra.helpers.DLibraDataSource;
import pl.psnc.dlibra.service.DLibraException;

/**
 * 
 * @author Piotr Hołubowicz
 *
 */
public class DigitalLibraryFactory
{

	private final static Logger logger = Logger
			.getLogger(DigitalLibraryFactory.class);

	private static int port;

	private static String host;

	private static long workspacesDirectory;

	private static long collectionId;


	public static DigitalLibrary getDigitalLibrary(UserCredentials creds)
		throws RemoteException, MalformedURLException, UnknownHostException
	{
		return getDigitalLibrary(creds.getUserId(), creds.getPassword());
	}


	public static DigitalLibrary getDigitalLibrary(String userLogin,
			String password)
		throws RemoteException, MalformedURLException, UnknownHostException
	{
		try {
			return new DLibraDataSource(host, port, workspacesDirectory,
					collectionId, userLogin, password);
		}
		catch (DLibraException e) {
			throw new RuntimeException(e.getMessage());
		}
	}


	public static void loadDigitalLibraryConfiguration(String configFileName)
	{
		logger.info("Loading connection properties file " + configFileName);
		InputStream inputStream = DigitalLibraryFactory.class.getClassLoader()
				.getResourceAsStream(configFileName);
		if (inputStream == null) {
			logger.error("Connection properties file not found! ");
			throw new RuntimeException("Connection properties file not found! ");
		}
		Properties properties = new Properties();
		try {
			properties.load(inputStream);
		}
		catch (IOException e) {
			logger.error("Unable to read connection properties", e);
			throw new RuntimeException("Unable to read connection properties",
					e);
		}
		finally {
			try {
				inputStream.close();
			}
			catch (IOException e) {
				//ignore
			}
		}
		host = properties.getProperty("host");
		port = Integer.parseInt(properties.getProperty("port"));
		logger.debug("Connection parameters: " + host + ":" + port);

		workspacesDirectory = Long.parseLong(properties
				.getProperty("workspacesDir"));
		logger.debug("Workspaces directory: " + workspacesDirectory);

		collectionId = Long.parseLong(properties.getProperty("collectionId"));
		logger.debug("Collection id: " + collectionId);
	}

}
