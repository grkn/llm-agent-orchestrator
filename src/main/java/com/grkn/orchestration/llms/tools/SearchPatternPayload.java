package com.grkn.orchestration.llms.tools;

import com.grkn.tool.library.annotation.ToolParameter;

public class SearchPatternPayload {
    @ToolParameter(description = "string pattern to be searched in the file")
    private String pattern;
    @ToolParameter(description = "file's absolute path to search")
    private String filePath;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
