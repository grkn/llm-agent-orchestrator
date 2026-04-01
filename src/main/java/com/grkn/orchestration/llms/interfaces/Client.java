package com.grkn.orchestration.llms.interfaces;

import com.grkn.orchestration.llms.dto.ApiResponse;
import com.grkn.orchestration.llms.properties.Properties;

import java.util.Map;

public interface Client {

    ApiResponse execute(Properties properties, String prompt, String responseId);
}
