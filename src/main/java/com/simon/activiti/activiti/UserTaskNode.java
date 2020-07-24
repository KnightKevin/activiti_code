package com.simon.activiti.activiti;

import lombok.Data;

@Data
public class UserTaskNode extends Node {
    private String id;
    private String assignee;
}
