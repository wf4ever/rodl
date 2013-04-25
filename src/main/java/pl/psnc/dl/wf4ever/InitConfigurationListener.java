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

import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

import pl.psnc.dl.wf4ever.monitoring.MonitoringScheduler;

/**
 * Initialize RODL on startup.
 * 
 * @author piotrekhol
 * 
 */
public class InitConfigurationListener implements ServletContextListener {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(InitConfigurationListener.class);


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ApplicationProperties.load();
        try {
            MonitoringScheduler.getInstance().start();
        } catch (SchedulerException e) {
            LOGGER.error("Can't start the RO monitoring scheduler", e);
        }
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            MonitoringScheduler.getInstance().stop();
        } catch (SchedulerException e) {
            LOGGER.error("Can't stop the RO monitoring scheduler", e);
        }
    }

}
