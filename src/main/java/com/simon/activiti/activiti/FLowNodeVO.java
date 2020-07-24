package com.simon.activiti.activiti;

import lombok.Data;

import java.util.List;

@Data
public class FLowNodeVO {
    private List<UserTaskNode> userTaskNodes;
    private List<SequenceFlowNode> sequenceFlowNodes;
}
