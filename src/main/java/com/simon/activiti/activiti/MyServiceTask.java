package com.simon.activiti.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyServiceTask implements JavaDelegate {
    Logger log = LoggerFactory.getLogger(MyServiceTask.class);
    @Override
    public void execute(DelegateExecution execution) {
        log.info("this is a ServiceTask "+execution.getProcessDefinitionId());
    }
}
