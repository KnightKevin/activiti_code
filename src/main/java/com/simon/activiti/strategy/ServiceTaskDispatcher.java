package com.simon.activiti.strategy;

import java.util.HashMap;
import java.util.Map;

public class ServiceTaskDispatcher {

    // 日后可以换成枚举类型，现在只是为了方便
    public final static String TYPE_VM = "vm";
    public final static String TYPE_VR = "vr";

    private String type;

    private static Map<String, BaseStrategy> strategyMap;

    // 初始化策略映射表
    static {
        strategyMap = new HashMap<>();
        strategyMap.put(TYPE_VM, new VmStrategy());
        strategyMap.put(TYPE_VR, new VrStrategy());
    }

    public ServiceTaskDispatcher(String type) {
        this.type = type;
    }

    public void doStrategy() {
        // 在这个地方需要组装策略所需要的参数

        strategyMap.get(type).execute();
    }
}
