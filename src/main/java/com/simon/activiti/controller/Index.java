package com.simon.activiti.controller;

import com.simon.activiti.activiti.FLowNodeVO;
import com.simon.activiti.enumn.FlowTypeEnum;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class Index {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @PostMapping("/create")
    public String create(@RequestBody FLowNodeVO node) {


        System.out.println("size is "+node.getUserTaskNodes().size());

        return "success";
    }

    @GetMapping("/show")
    public String show(FlowTypeEnum flowTypeEnum) {

        System.out.println("enum is "+flowTypeEnum.getValue());
        return "ok";
    }
}
