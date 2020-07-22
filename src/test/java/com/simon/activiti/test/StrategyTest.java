package com.simon.activiti.test;

import com.simon.activiti.strategy.ServiceTaskDispatcher;
import org.junit.Test;

public class StrategyTest extends ApplicationTest {

    @Test
    public void doStrategyTest() {
        ServiceTaskDispatcher dispatcher = new ServiceTaskDispatcher(ServiceTaskDispatcher.TYPE_VM);
        dispatcher.doStrategy();

        dispatcher = new ServiceTaskDispatcher(ServiceTaskDispatcher.TYPE_VR);
        dispatcher.doStrategy();
    }
}
