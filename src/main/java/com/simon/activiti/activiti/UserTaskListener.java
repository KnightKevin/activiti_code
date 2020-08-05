package com.simon.activiti.activiti;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserTaskListener implements TaskListener {
    Logger log = LoggerFactory.getLogger(UserTaskListener.class);

    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("事件监听启动了，当前的assignee是"+delegateTask.getAssignee());

    }
}