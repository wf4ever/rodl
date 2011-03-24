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

/**
 * 
 * @author nowakm
 *
 */
public class DlibraConnectionRegistry
{

	private static DlibraConnectionRegistry soleInstance;

	private DLibraConnection connection;


	public DlibraConnectionRegistry(DLibraConnection connection)
	{
		this.connection = connection;
	}


	public static DLibraConnection getConnection()
	{
		if (soleInstance == null) {
			throw new NullPointerException(
					"dLibra connection registry has not been loaded. ");
		}

		return soleInstance.connection;
	}


	public static void loadRegistry(DlibraConnectionRegistry registry)
	{
		soleInstance = registry;
	}

}
