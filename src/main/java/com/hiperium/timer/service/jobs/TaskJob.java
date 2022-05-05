package com.hiperium.timer.service.jobs;

import com.hiperium.timer.service.utils.TaskJobUtil;
import org.jboss.logging.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

/**
 * @author Andres Solorzano
 */
public class TaskJob implements Job {

    private static final Logger LOGGER = Logger.getLogger(TaskJob.class.getName());

    @Override
    public void execute(JobExecutionContext executionContext) {
        LOGGER.debug("execute() - START");
        LOGGER.debug("Task ID: " + executionContext.getJobDetail().getJobDataMap()
                .get(TaskJobUtil.TASK_ID_DATA_KEY));
        LOGGER.debug("Task Command: " + executionContext.getJobDetail().getJobDataMap()
                .get(TaskJobUtil.TASK_COMMAND_DATA_KEY));
        LOGGER.debug("execute() - END");
    }
}
