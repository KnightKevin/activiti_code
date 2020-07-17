package com.simon.activiti.enumn;

public enum  FlowTypeEnum {
    vm(1),vr(2);
    private Integer value;
    private FlowTypeEnum(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
