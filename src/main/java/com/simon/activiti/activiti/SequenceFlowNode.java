package com.simon.activiti.activiti;

import lombok.Data;

@Data
public class SequenceFlowNode extends Node {
    private String source;
    private String target;
}
