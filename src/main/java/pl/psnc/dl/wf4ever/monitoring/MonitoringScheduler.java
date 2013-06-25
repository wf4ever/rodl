package pl.psnc.dl.wf4ever.monitoring;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.NameMatcher;

/**
 * This class manages the jobs and triggers.
 * 
 * @author piotrekhol
 * 
 */
public final class MonitoringScheduler {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(MonitoringScheduler.class);

    /** The only instance. */
    private static MonitoringScheduler instance = null;

    /** A scheduler factory. */
    private static SchedulerFactory schedulerFactory = new StdSchedulerFactory();

    /** The main scheduler. */
    private Scheduler scheduler;

    /** Jobs loaded from the config. */
    private Map<Class<? extends Job>, ScheduleBuilder<? extends Trigger>> jobClasses = new HashMap<>();


    /**
     * A private constructor.
     */
    private MonitoringScheduler() {
    }


    /**
     * Get the only instance of this class.
     * 
     * @return the only instance of this class
     */
    public static MonitoringScheduler getInstance() {
        if (instance == null) {
            instance = new MonitoringScheduler();
        }
        return instance;
    }


    /**
     * Initialize all jobs.
     * 
     * @throws SchedulerException
     *             can't initialize the Quartz scheduler
     */
    @SuppressWarnings("unchecked")
    public void start()
            throws SchedulerException {
        scheduler = schedulerFactory.getScheduler();
        scheduler.start();
        String[] plugins = scheduler.getContext().getString("plugins").split(",");
        for (String pluginName : plugins) {
            pluginName = pluginName.trim();
            String pluginClassName = scheduler.getContext().getString(pluginName + ".class");
            Class<? extends Job> pluginClass;
            try {
                pluginClass = (Class<? extends Job>) Class.forName(pluginClassName);
            } catch (ClassNotFoundException e) {
                LOGGER.error("Invalid plugin class", e);
                continue;
            }
            ScheduleBuilder<? extends Trigger> schedule = createSchedule(pluginName);
            jobClasses.put(pluginClass, schedule);
            scheduleJob(pluginName, pluginClass, schedule);

            JobListener jobListener = createJobListener(pluginName);
            if (jobListener != null) {
                try {
                    scheduler.getListenerManager().addJobListener(jobListener,
                        NameMatcher.<JobKey> nameEquals(pluginName));
                } catch (SchedulerException e) {
                    LOGGER.error("Can't add the job listener", e);
                }
            }

        }
    }


    /**
     * Find the schedule config and create an instance.
     * 
     * @param pluginName
     *            plugin name to use when searching the config
     * @return a cron schedule or a simple schedule if no config found
     * @throws SchedulerException
     *             when it can't get the context
     */
    private ScheduleBuilder<? extends Trigger> createSchedule(String pluginName)
            throws SchedulerException {
        String cron = scheduler.getContext().getString(pluginName + ".cron");
        return cron != null ? cronSchedule(cron).withMisfireHandlingInstructionIgnoreMisfires() : simpleSchedule();
    }


    /**
     * Find the listener config and create an instance.
     * 
     * @param pluginName
     *            plugin name to use when searching the config
     * @return a job listener or null if can't create
     * @throws SchedulerException
     *             when it can't get the context
     */
    @SuppressWarnings("unchecked")
    private JobListener createJobListener(String pluginName)
            throws SchedulerException {
        String listenerClassName = scheduler.getContext().getString(pluginName + ".listenerclass");
        if (listenerClassName == null || listenerClassName.trim().isEmpty()) {
            return null;
        }
        Class<? extends JobListener> listenerClass;
        try {
            listenerClass = (Class<? extends JobListener>) Class.forName(listenerClassName);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Invalid job listener class", e);
            return null;
        }
        JobListener jobListener;
        try {
            jobListener = listenerClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            LOGGER.error("Can't create the job listener", e);
            return null;
        }
        return jobListener;
    }


    /**
     * Schedule all registered jobs to run now.
     * 
     * @throws SchedulerException
     *             if the job cannot be scheduled
     */
    public void scheduleAllJobsNow()
            throws SchedulerException {
        for (Entry<Class<? extends Job>, ScheduleBuilder<? extends Trigger>> e : jobClasses.entrySet()) {
            scheduleJob(e.getKey().getCanonicalName(), e.getKey());
        }
    }


    /**
     * Schedule a job to run now.
     * 
     * @param id
     *            job name
     * @param jobClass
     *            job class
     * @throws SchedulerException
     *             if the job cannot be scheduled
     */
    public void scheduleJob(String id, Class<? extends Job> jobClass)
            throws SchedulerException {
        scheduleJob(id, jobClass, simpleSchedule());
    }


    /**
     * Schedule a job.
     * 
     * @param id
     *            job name
     * @param jobClass
     *            job class
     * @param schedule
     *            when to run the job
     * @throws SchedulerException
     *             if the job cannot be scheduled
     */
    public void scheduleJob(String id, Class<? extends Job> jobClass, ScheduleBuilder<? extends Trigger> schedule)
            throws SchedulerException {
        JobDetail job = newJob(jobClass).withIdentity(id).build();
        Trigger trigger = newTrigger().withSchedule(schedule).build();
        scheduler.scheduleJob(job, trigger);
    }


    /**
     * Stop all jobs.
     * 
     * @throws SchedulerException
     *             can't stop the Quartz scheduler
     */
    public void stop()
            throws SchedulerException {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }


    public static SchedulerFactory getSchedulerFactory() {
        return schedulerFactory;
    }


    public static void setSchedulerFactory(SchedulerFactory schedulerFactory) {
        MonitoringScheduler.schedulerFactory = schedulerFactory;
    }
}
