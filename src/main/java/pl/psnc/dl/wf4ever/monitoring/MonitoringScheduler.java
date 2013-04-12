package pl.psnc.dl.wf4ever.monitoring;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;

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
        SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();

        Scheduler sched = schedFact.getScheduler();
        String[] plugins = sched.getContext().getString("plugins").split(",");
        for (String plugin : plugins) {
            String pluginClassName = sched.getContext().getString(plugin.trim() + ".class");
            Class<? extends Job> pluginClass;
            try {
                pluginClass = (Class<? extends Job>) Class.forName(pluginClassName);
            } catch (ClassNotFoundException e) {
                LOGGER.error("Invalid plugin class", e);
                continue;
            }
            String cron = sched.getContext().getString(plugin.trim() + ".cron");
            JobDetail job = newJob(pluginClass).withIdentity(plugin).build();
            Trigger trigger = newTrigger().withSchedule(
                cronSchedule(cron).withMisfireHandlingInstructionIgnoreMisfires()).build();
            sched.scheduleJob(job, trigger);
        }

        sched.start();
    }
}
