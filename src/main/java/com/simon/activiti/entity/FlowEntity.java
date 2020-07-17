package com.simon.activiti.entity;

import lombok.Data;

@Data
public class FlowEntity {
    private int id;
    private int type;
    private int formId;
    private String createBy;
    private String processInstanceId;
}
