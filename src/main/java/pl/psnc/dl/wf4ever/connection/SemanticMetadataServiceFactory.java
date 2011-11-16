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
import java.sql.SQLException;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl;

/**
 * 
 * @author Piotr Hołubowicz
 *
 */
public class SemanticMetadataServiceFactory
{

	@SuppressWarnings("unused")
	private final static Logger logger = Logger
			.getLogger(SemanticMetadataServiceFactory.class);


	public static SemanticMetadataService getService(UserProfile userProfile)
		throws ClassNotFoundException, IOException, NamingException,
		SQLException
	{
		return new SemanticMetadataServiceImpl();
	}

}
