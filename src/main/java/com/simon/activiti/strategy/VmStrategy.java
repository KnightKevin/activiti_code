package com.simon.activiti.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmStrategy implements BaseStrategy {
    Logger log = LoggerFactory.getLogger(VrStrategy.class);

    @Override
    public void execute() {
        log.info("vm strategy");
    }
}
