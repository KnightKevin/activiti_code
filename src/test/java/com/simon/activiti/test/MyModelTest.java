package com.simon.activiti.test;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 利用BpmnModel来部署
 * */
public class MyModelTest extends ApplicationTest {

    private Logger log = LoggerFactory.getLogger(VacationTest.class);

    private DeploymentBuilder builder;

    private final  static String key = "flowBeta";

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
        builder.addBpmnModel(key+"bpmn20.xml", createProcessModel())
                .name("Manual")
                .key("manual")
                .enableDuplicateFiltering()
                .deploy()
        ;
    }

    @Test
    public void startProcess() {
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(key);
        log.info("process instance id is "+instance.getId());
    }

    /**
     * 完成任务case
     * */
    @Test
    public void completeTask() {
        Task task = taskService.createTaskQuery().active().taskAssignee("Simon").singleResult();

        Map<String, Object> map = new HashMap<>();
        map.put("isPass", false);

        taskService.complete(task.getId(), map);
        log.info("task id = "+task);
    }

    /**
     * 自己解析出我需要的流程节点后构建出一个我自己设计的数据结构可以作为接口请求参数也可以作为给前端绘图用的数据结构
     * */
    @Test
    public void getProcessToJson() {
        // 准确来说应该是获取实例所关联的流程定义
        List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery().processDefinitionKey(key).list();
        BpmnModel bpmnModel = null;

        log.info("the size of process instance list is "+instances.size());

        List<UserTaskParam> userTaskList = new LinkedList<>();
        List<SequenceFlowParam> sequenceFlowList = new LinkedList<>();
        FlowBpmnParam flowBpmnParam = new FlowBpmnParam();

        for (HistoricProcessInstance instance : instances) {
            log.info("definitionKey       = "+instance.getProcessDefinitionKey());
            log.info("version             = "+instance.getProcessDefinitionVersion());
            log.info("processDefinitionId = "+instance.getProcessDefinitionId());
            log.info("deploymentId        = "+instance.getDeploymentId());

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
    public void bpmnModelToXml() {
        BpmnXMLConverter converter = new BpmnXMLConverter();
        byte[] xml = converter.convertToXML(createProcessModel());
        log.info(new String(xml));
    }

    public BpmnModel createProcessModel() {
        BpmnModel model = new BpmnModel();
        model.setTargetNamespace("http://activiti.org/bpmn20");

        Process process = new Process();
        process.setId(key);
        process.setName("服务目录流程模板 beta1.1");

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

        UserTask userTask1 = new UserTask();
        userTask1.setName("处理请求");
        userTask1.setId("handleRequest1");
        userTask1.setAssignee("admin");
        process.addFlowElement(userTask1);

        // exclusive gateway
        ExclusiveGateway gateway = new ExclusiveGateway();
        gateway.setId("exclusiveGate1");
        gateway.setName("第一个单向网关");
        process.addFlowElement(gateway);

        ExclusiveGateway gateway1 = new ExclusiveGateway();
        gateway1.setId("exclusiveGate2");
        gateway1.setName("第二个单向网关");
        process.addFlowElement(gateway1);

        // 结束事件
        EndEvent passEndEvent = new EndEvent();
        passEndEvent.setId("passEndEvent");
        passEndEvent.setName("结束-通过");
        process.addFlowElement(passEndEvent);

        EndEvent notPassEndEvent = new EndEvent();
        notPassEndEvent.setId("notPassEndEvent");
        notPassEndEvent.setName("结束-未通过");
        process.addFlowElement(notPassEndEvent);

        // 添加流程顺序
        process.addFlowElement(new SequenceFlow(startEvent.getId(), userTask.getId()));

        process.addFlowElement(new SequenceFlow(userTask.getId(), gateway.getId()));

        SequenceFlow sequenceFlow = new SequenceFlow(gateway.getId(), notPassEndEvent.getId());
        sequenceFlow.setConditionExpression("${isPass == 'false'}");
        process.addFlowElement(sequenceFlow);

        sequenceFlow = new SequenceFlow(gateway.getId(), userTask1.getId());
        sequenceFlow.setConditionExpression("${isPass == 'true'}");
        process.addFlowElement(sequenceFlow);

        process.addFlowElement(new SequenceFlow(userTask1.getId(), gateway1.getId()));

        sequenceFlow = new SequenceFlow(gateway1.getId(), notPassEndEvent.getId());
        sequenceFlow.setConditionExpression("${isPass == 'false'}");
        process.addFlowElement(sequenceFlow);

        sequenceFlow = new SequenceFlow(gateway1.getId(), passEndEvent.getId());
        sequenceFlow.setConditionExpression("${isPass == 'true'}");
        process.addFlowElement(sequenceFlow);

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
