package com.simon.activiti.test;

import com.alibaba.fastjson.JSON;
import com.simon.activiti.activiti.*;
import lombok.Data;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.*;

/**
 * 利用BpmnModel来部署
 * */
public class MyModelTest extends ApplicationTest {

    private Logger log = LoggerFactory.getLogger(MyModelTest.class);

    private DeploymentBuilder builder;

    private final  static String key = "flowBeta";

    private final List<ActivitiListener> taskListenerList = new ArrayList<>();

    @Before
    public void before() {
        builder = repositoryService.createDeployment();
        ActivitiListener listener = new ActivitiListener();
        listener.setEvent(TaskListener.EVENTNAME_CREATE);
        listener.setImplementation(UserTaskListener.class.getName());
        listener.setImplementationType("class");
        taskListenerList.add(listener);
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
        identityService.setAuthenticatedUserId("admin");
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(key);
        log.info("process instance id is "+instance.getId());
    }

    /**
     * 完成任务case
     * */
    @Test
    public void completeTask() {

        Map<String, Object> map = new HashMap<>();
        map.put("isPass", true);

        String taskId = "15009";
        taskService.complete(taskId, map);
        log.info("task id = "+taskId);
    }

    /**
     * 自己解析出我需要的流程节点后构建出一个我自己设计的数据结构可以作为接口请求参数也可以作为给前端绘图用的数据结构
     * */
    @Test
    public void getProcessToJson() {
        // 准确来说应该是获取实例所关联的流程定义
        List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(key)
                .list();
        BpmnModel bpmnModel = null;

        log.info("the size of process instance list is "+instances.size());

        FlowBpmnParam flowBpmnParam = new FlowBpmnParam();

        List<Node> nodeList = new ArrayList<>();

        for (HistoricProcessInstance instance : instances) {
            log.info("definitionKey       = "+instance.getProcessDefinitionKey());
            log.info("version             = "+instance.getProcessDefinitionVersion());
            log.info("processDefinitionId = "+instance.getProcessDefinitionId());
            log.info("deploymentId        = "+instance.getDeploymentId());

            bpmnModel = repositoryService.getBpmnModel(instance.getProcessDefinitionId());
            Process process = bpmnModel.getProcessById(key);

            Collection<FlowElement> list = process.getFlowElements();
            log.info("the size of map is "+list.size());


            for (FlowElement element : list) {
                if (element instanceof UserTask) {
                    UserTaskNode userTaskNode = new UserTaskNode();
                    userTaskNode.setType("UserTask");
                    userTaskNode.setId(element.getId());
                    userTaskNode.setAssignee(((UserTask) element).getAssignee());
                    nodeList.add(userTaskNode);
                }

                if (element instanceof SequenceFlow) {
                    SequenceFlowNode node = new SequenceFlowNode();
                    node.setType("SequenceFlow");
                    node.setSource(((SequenceFlow) element).getSourceRef());
                    node.setTarget(((SequenceFlow) element).getTargetRef());
                    nodeList.add(node);
                }
            }

            flowBpmnParam.setList(nodeList);

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

    @Test
    public void xmlToBpmn() throws FileNotFoundException, XMLStreamException {
        // xml文件读取
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        Reader reader = new FileReader("D:\\work_space\\activiti-code\\src\\main\\resources\\tmp\\Flow.bpmn20.xml");

        XMLStreamReader xmlStreamReader = inputFactory.createXMLStreamReader(reader);

        BpmnXMLConverter converter = new BpmnXMLConverter();
        BpmnModel model = converter.convertToBpmnModel(xmlStreamReader);

        List<Process> list = model.getProcesses();
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
        userTask.setTaskListeners(taskListenerList); // 添加此监听器的目的是为了防止人员变动，比如原来指定的人不在了，可以换成其他人
        process.addFlowElement(userTask);

        UserTask userTask1 = new UserTask();
        userTask1.setName("处理请求");
        userTask1.setId("handleRequest1");
        userTask1.setAssignee("admin");
        process.addFlowElement(userTask1);

        // service task
        ServiceTask serviceTask = new ServiceTask();
        serviceTask.setId("handleServiceTask");
        serviceTask.setName("执行服务任务");
        serviceTask.setImplementation(MyServiceTask.class.getName());
        serviceTask.setImplementationType("class");
        process.addFlowElement(serviceTask);

        // 设置一个边界事件来捕获service task的错误，并记下日志
//        BoundaryEvent boundaryEvent = new BoundaryEvent();
//        boundaryEvent.setAttachedToRefId(serviceTask.getExtensionId());
//        boundaryEvent.setCancelActivity(false);
//        boundaryEvent.addEventDefinition(new ErrorEventDefinition());

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

        sequenceFlow = new SequenceFlow(gateway1.getId(), serviceTask.getId());
        sequenceFlow.setConditionExpression("${isPass == 'true'}");
        process.addFlowElement(sequenceFlow);

        process.addFlowElement(new SequenceFlow(serviceTask.getId(), passEndEvent.getId()));

        return model;
    }

}

// 定义几个实体

@Data
class FlowBpmnParam {
    private String startEvent = "startEvent";
    private String endEvent   = "endEvent";
    private List<Node> list;
}