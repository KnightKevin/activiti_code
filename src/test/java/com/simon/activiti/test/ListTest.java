package com.simon.activiti.test;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListTest extends ApplicationTest {

    private static final Logger log = LoggerFactory.getLogger(ListTest.class);

    @Autowired
    HistoryService historyService;

    @Autowired
    TaskService taskService;

    @Autowired
    ManagementService managementService;

    private static final String key = "flowBeta";


    @Test
    public void listMyProcess() {
        String uid = "admin";

        List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
                .startedBy(uid)
                .list()
                ;

        for (HistoricProcessInstance instance : instances) {
            log.info(
                    "processInstanceId=" +instance.getId()
                    +" processDefinitionKey=" +instance.getProcessDefinitionId()
                    +" startBy="+instance.getStartUserId()
            );
        }
    }

    @Test
    public void listMyTask() {
        String uid = "Simon";
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(uid).active().list();

        for (Task task : tasks) {
            log.info(
                    "processInstanceId=" +task.getProcessInstanceId()
                            +" processDefinitionKey=" +task.getProcessDefinitionId()
                            +" assignee="+task.getAssignee()
            );
        }

    }


    @Test
    public void listParticipateProcess() {
        String uid = "Simon";


        // 只能用native

        log.info("table is "+managementService.getTableName(HistoricActivityInstance.class));
        List<HistoricActivityInstance> list = historyService.createNativeHistoricActivityInstanceQuery().sql(
                "SELECT any_value(id_) id_, proc_inst_id_ from "
                        +managementService.getTableName(HistoricActivityInstance.class)
                        +" WHERE assignee_ = #{assignee}"
                +" group by proc_inst_id_"
        )
                .parameter("assignee", uid)
                .list();

        Set<String> ids = new HashSet<>(list.size());
        for (HistoricActivityInstance instance: list) {
            ids.add(instance.getProcessInstanceId());
        }

        List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
                .processInstanceIds(ids)
                .list()
                ;

        log.info(instances.size()+"");

    }
}
