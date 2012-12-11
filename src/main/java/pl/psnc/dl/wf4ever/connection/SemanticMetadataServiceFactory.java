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

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceTdb;

/**
 * A factory for creating a connection to the Semantic Metadata Service.
 * 
 * @author Piotr Hołubowicz
 * 
 */
public final class SemanticMetadataServiceFactory {

    /** logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(SemanticMetadataServiceFactory.class);


    /**
     * Private constructor.
     */
    private SemanticMetadataServiceFactory() {
        //nope
    }


    /**
     * Get the SMS.
     * 
     * @param user
     *            user identity
     * @return SMS
     * @throws ClassNotFoundException
     *             could not create the SMS
     * @throws IOException
     *             could not create the SMS
     * @throws NamingException
     *             could not create the SMS
     * @throws SQLException
     *             could not create the SMS
     */
    public static SemanticMetadataService getService(UserMetadata user)
            throws ClassNotFoundException, IOException, NamingException, SQLException {
        return new SemanticMetadataServiceTdb(user, true);
    }

}
