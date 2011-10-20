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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author nowakm
 *
 */
public class DlibraConnectionConfigurationListener
	implements ServletContextListener
{

	private static final String CONNECTION_PROPERTIES_FILENAME = "connection.properties.filename";


	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		ServletContext servletContext = sce.getServletContext();

		String fileName = servletContext
				.getInitParameter(CONNECTION_PROPERTIES_FILENAME);
		DigitalLibraryFactory.loadDigitalLibraryConfiguration(fileName);
	}


	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		// nothing to do
	}
}
