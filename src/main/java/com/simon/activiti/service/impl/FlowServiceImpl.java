package com.simon.activiti.service.impl;

import com.simon.activiti.entity.FlowEntity;
import com.simon.activiti.mapper.FlowMapper;
import com.simon.activiti.service.FlowService;
import org.springframework.beans.factory.annotation.Autowired;

public class FlowServiceImpl implements FlowService {

    @Autowired
    FlowMapper flowMapper;

    @Override
    public void create(FlowEntity flowEntity) {
        // 第一件事根据type判断是要处理哪个表单，当然可能不存在表单,表单可以为空的
        flowEntity.getType();

        switch (flowEntity.getType()) {
        }
    }

}
