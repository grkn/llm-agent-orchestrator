package com.grkn.orchestration.llms.interfaces;

import java.lang.reflect.Method;
import java.util.List;

public interface ToolFinder {

    Method find(String toolName);
}
