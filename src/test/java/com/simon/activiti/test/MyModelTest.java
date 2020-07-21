package com.simon.activiti.test;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 利用BpmnModel来部署
 * */
public class MyModelTest extends ApplicationTest {

    private Logger log = LoggerFactory.getLogger(VacationTest.class);

    private DeploymentBuilder builder;

    private final  static String key = "MyRequest";

    @Before
    public void hhh() {
        builder = repositoryService.createDeployment();
    }

    /**
     * 通过BpmnModel来部署流程定义，切记resourceName参数一定要加bpmn20.xml或者bpmn后缀
     * 不然就会部署个寂寞
     * */
    @Test
    public void deployBpmnModelProcess() {
        builder.addBpmnModel("MyRequest.bpmn20.xml", createProcessModel())
                .name("Manual")
                .key("manual")
                .enableDuplicateFiltering()
                .deploy()
        ;
    }

    /**
     * 自己解析出我需要的流程节点后构建出一个我自己设计的数据结构可以作为接口请求参数也可以作为给前端绘图用的数据结构
     * */
    @Test
    public void getProcessToJson() {
        // 准确来说应该是获取实例所关联的流程定义
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().processDefinitionKey(key).list();
        ProcessDefinition processDefinition = null;
        BpmnModel bpmnModel = null;

        List<UserTaskParam> userTaskList = new LinkedList<>();
        List<SequenceFlowParam> sequenceFlowList = new LinkedList<>();
        FlowBpmnParam flowBpmnParam = new FlowBpmnParam();

        for (ProcessInstance instance : instances) {
            log.info("definitionKey       = "+instance.getProcessDefinitionKey());
            log.info("version             = "+instance.getProcessDefinitionVersion());
            log.info("processDefinitionId = "+instance.getProcessDefinitionId());
            log.info("deploymentId = "+instance.getDeploymentId());

            bpmnModel = repositoryService.getBpmnModel(instance.getProcessDefinitionId());
            Process process = bpmnModel.getProcessById(key);

            Collection<FlowElement> list = process.getFlowElements();
            log.info("the size of map is "+list.size());

            userTaskList.clear();
            sequenceFlowList.clear();

            for (FlowElement element : list) {
                if (element instanceof UserTask) {
                    userTaskList.add(new UserTaskParam(element.getId(), ((UserTask) element).getAssignee()));
                }

                if (element instanceof SequenceFlow) {
                    sequenceFlowList.add(new SequenceFlowParam(((SequenceFlow) element).getSourceRef(), ((SequenceFlow) element).getTargetRef()));
                }
            }

            flowBpmnParam.setUserTaskList(userTaskList);
            flowBpmnParam.setSequenceFlowList(sequenceFlowList);

            log.info(JSON.toJSONString(flowBpmnParam));
            log.info("######################################################");
        }




    }

    @Test
    public void deployProcessByXml() {
        builder.addClasspathResource("tmp/purchase.bpmn")
                .name("Manual")
                .enableDuplicateFiltering().deploy();
    }

    @Test
    public void queryProcessDef() {
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();

        for (ProcessDefinition definition : list) {
            log.info("lkjasdf " + definition.getName());
        }
    }

    @Test
    public void startProcess() {
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(key);
        log.info("process instance id is "+instance.getId());
    }

    public BpmnModel createProcessModel() {
        BpmnModel model = new BpmnModel();

        Process process = new Process();
        process.setId(key);
        process.setName("自己的模型对象-hahaha");

        model.addProcess(process);

        // 开始事件
        StartEvent startEvent = new StartEvent();
        startEvent.setId("startEvent");
        process.addFlowElement(startEvent);

        // user task
        UserTask userTask = new UserTask();
        userTask.setName("处理请求");
        userTask.setId("handleRequest");
        userTask.setAssignee("Simon");
        process.addFlowElement(userTask);

        // 结束事件
        EndEvent endEvent = new EndEvent();
        endEvent.setId("endEvent");
        endEvent.setName("endEvent");
        process.addFlowElement(endEvent);

        // 添加流程顺序
        process.addFlowElement(new SequenceFlow("startEvent", "handleRequest"));
        process.addFlowElement(new SequenceFlow("handleRequest", "endEvent"));

        return model;
    }

}

// 定义几个实体
@Data
class UserTaskParam {
    private String id;
    private String assignee ="";
    public UserTaskParam(String id, String assignee) {
        this.id = id;
        this.assignee = assignee;
    }
}

@Data
class SequenceFlowParam {
    private String source;
    private String target;
    public SequenceFlowParam(String source, String target) {
        this.source = source;
        this.target = target;
    }
}

@Data
class FlowBpmnParam {
    private String startEvent = "startEvent";
    private String endEvent   = "endEvent";
    private List<UserTaskParam> userTaskList;
    private List<SequenceFlowParam> sequenceFlowList;
}
