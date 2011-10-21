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

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;

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
	{
		return null;
	}

}
