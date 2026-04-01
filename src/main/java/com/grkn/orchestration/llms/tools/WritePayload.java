package com.grkn.orchestration.llms.tools;

import com.grkn.tool.library.annotation.ToolParameter;

public class WritePayload {
    @ToolParameter(description = "file's absolute path to write")
    private String absolutePath;

    @ToolParameter(description = "content of file to be written")
    private String content;

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
