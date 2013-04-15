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
package pl.psnc.dl.wf4ever;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * Read in the dLibra connection parameters on startup.
 * 
 * @author nowakm
 * 
 */
public class InitConfigurationListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ApplicationProperties.load();
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing to do
    }

}
