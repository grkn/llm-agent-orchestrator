package com.grkn.orchestration.llms.interfaces;

import com.grkn.orchestration.llms.enums.Action;
import com.grkn.orchestration.llms.fsm.AbstractAgent;

import java.util.List;
import java.util.Map;

public interface ToolRunner<T> {

    T run(Action action, List<String> toolNames, List<Map<String, String>> inputs, AbstractAgent agent) throws InterruptedException;
}
