package com.hiperium.timer.service.jobs;

import com.hiperium.timer.service.services.TaskService;
import com.hiperium.timer.service.utils.TaskJobUtil;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import javax.inject.Inject;

/**
 * @author Andres Solorzano
 */
@RegisterForReflection
public class TaskJob implements Job {

    @Inject
    TaskService taskService;

    public TaskJob() {
        // Nothing to implement
    }

    @Override
    public void execute(JobExecutionContext executionContext) {
        String taskId = (String) executionContext.getJobDetail().getJobDataMap()
                .get(TaskJobUtil.TASK_ID_DATA_KEY);
        this.taskService.executeTask(taskId);
    }
}
