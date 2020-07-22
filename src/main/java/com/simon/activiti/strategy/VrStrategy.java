package com.simon.activiti.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VrStrategy implements BaseStrategy {
    Logger log = LoggerFactory.getLogger(VrStrategy.class);
    @Override
    public void execute() {
        log.info("vr strategy");
        // 需要根据所传参数决定我去查那些数据，并执行接下来的操作
        // 比如构建云主机需要用到cpu，mem参数
        // 构建其他的，有需要另外的参数
        // 至少我知道我肯定要知道那些数据的
    }
}
