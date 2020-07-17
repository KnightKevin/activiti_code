package com.simon.activiti.test;

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

import java.util.List;

/**
 * 利用BpmnModel来部署
 * */
public class MyModelTest extends ApplicationTest {

    private Logger log = LoggerFactory.getLogger(VacationTest.class);

    private DeploymentBuilder builder;

    @Before
    public void hhh() {
        builder = repositoryService.createDeployment();
    }


    @Test
    public void deployProcess() {
        builder.addBpmnModel("MyRequest", createProcessModel())
                .name("Manual")
                .enableDuplicateFiltering()
                .deploy()
        ;
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
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("MyRequest");
        log.info("process instance id is "+instance.getId());
    }

    @Test
    public void createXml() {
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
        byte[] xml = bpmnXMLConverter.convertToXML(createProcessModel());
        log.info(new String(xml));
    }

    public BpmnModel createProcessModel() {
        BpmnModel model = new BpmnModel();

        Process process = new Process();
        process.setId("MyRequest");
        process.setName("自己的模型对象");

        model.addProcess(process);

        // 开始事件
        StartEvent startEvent = new StartEvent();
        startEvent.setId("startEvent");
        process.addFlowElement(startEvent);

        // user task
        UserTask userTask = new UserTask();
        userTask.setName("处理请求");
        userTask.setId("handleRequest");
        process.addFlowElement(userTask);

        // 结束事件
        EndEvent endEvent = new EndEvent();
        endEvent.setId("endEvent");
        process.addFlowElement(endEvent);

        // 添加流程顺序
        process.addFlowElement(new SequenceFlow("startEvent", "handleRequest"));
        process.addFlowElement(new SequenceFlow("handleRequest", "endEvent"));

        return model;
    }

}
