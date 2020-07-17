package com.simon.activiti.test;

import org.activiti.engine.IdentityService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.tomcat.jni.Proc;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VacationTest extends ApplicationTest {

    private Logger log = LoggerFactory.getLogger(VacationTest.class);

    private static final String key="vacationRequest";

    @Test
    public void start() {
        Map<String, Object> map = new HashMap<>();
        map.put("numberOfDays", 5);
        map.put("startDate", "2012-10-10");
        map.put("vacationMotivation", "rest!!!!");

        identityService.setAuthenticatedUserId("Admin");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key, map);
        log.info("开启了一个process instance，id为："+processInstance.getId()+ " activityId="+processInstance.getActivityId());

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();

        for (Task task:tasks) {
            log.info("ljjk "+ task.getName());
        }

        map.clear();
//        map.put("vacationApproved", false);
//        map.put("managerMotivation","hahahahah!");
//
//        taskService.claim(task.getId(), "manager");
//        taskService.complete(task.getId(), map);

    }

    @Test
    public void query() {
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().processDefinitionKey("vacationRequest").list();
        for (ProcessInstance instance : instances) {
            log.info("ljaksjfdl "+instance.getActivityId());
        }
    }

    @Test
    public void suspend() {
        String id = "2516";
        runtimeService.suspendProcessInstanceById(id);
        log.info("暂停了"+id+"流程");
    }

    @Test
    public void testt() {
        String id = "2516";

        // 获得暂停的流程的当前任务，并尝试去执行他
        Task task = taskService.createTaskQuery().processInstanceId(id).suspended().singleResult();


        Map<String,Object> map = new HashMap<>();
        map.put("vacationApproved", false);
        map.put("managerMotivation","hahahahah!");
        taskService.complete(task.getId(), map);

        log.info("task 的状态为 "+task);
    }
}
