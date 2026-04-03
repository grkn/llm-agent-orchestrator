package com.grkn.orchestration.llms.tools;

import com.grkn.tool.library.annotation.Tool;
import com.grkn.tool.library.annotation.ToolParameter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Tool
public class Tools {

    @Tool(name = "READ_FILE", description = """
            Read a file with given path
            """)
    public String readFile(@ToolParameter(description = """
            Payload is path you need to read
            """) String path) throws IOException {
        File f = new File(path);
        FileInputStream fileInputStream = new FileInputStream(f);
        byte[] bytes = fileInputStream.readAllBytes();
        fileInputStream.close();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Tool(name = "WRITE_FILE", description = """
            Write a file with given path
            """)
    public String writeFile(@ToolParameter(description = """
            Composite payload with filePath and content
            """) WritePayload writePayload) throws IOException {
        createDirectory(writePayload.getAbsolutePath());
        File f = new File(writePayload.getAbsolutePath());
        FileOutputStream fileOutputStream = new FileOutputStream(f);
        fileOutputStream.write(writePayload.getContent().getBytes(StandardCharsets.UTF_8));
        fileOutputStream.close();
        return "write operation done";
    }

    @Tool(name = "LIST_FILE", description = """
            List files with given path
            """)
    public String listFiles(@ToolParameter(description = """
            absolute file path you need to list
            """) String path) throws IOException {
        Path root = Paths.get(path);
        String content = "";
        try (var stream = Files.walk(root, 10)) {
            content = stream.filter(Files::isRegularFile)
                    .filter(this::isRelevantFile)
                    .limit(500)
                    .map(Path::toString)
                    .collect(Collectors.joining("\n"));
        }
        return content;
    }

    @Tool(name = "SEARCH_PATTERN_IN_FILE", description = """
            Search Pattern inside a file with given path and returns found file's path
            """)
    public String searchPatternInFile(@ToolParameter(description = """
            Search Pattern payload
            """) SearchPatternPayload searchPatternPayload) throws IOException {
        File f = new File(searchPatternPayload.getFilePath());
        FileInputStream fileInputStream = new FileInputStream(f);
        byte[] bytes = fileInputStream.readAllBytes();
        fileInputStream.close();
        String content = new String(bytes, StandardCharsets.UTF_8);
        return content.contains(searchPatternPayload.getPattern()) ? f.getAbsolutePath() : "Pattern not found";
    }

    @Tool(name = "REWRITE_FILE", description = """
            Update file content by rewriting with given content and file's absolute path.
            """)
    public String rewrite(@ToolParameter(description = """
            Rewrite file with given path and new content
            """) WritePayload writePayload) {
        String payload = "File rewrite operation done";
        File f = new File(writePayload.getAbsolutePath());
        try (FileOutputStream fileOutputStream = new FileOutputStream(f)) {
            fileOutputStream.write(writePayload.getContent().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            payload = "File rewrite operation failed";
        }
        return payload;
    }

    @Tool(name = "DELETE_FILE", description = """
            Delete unnecessary file with given path
            """)
    public String deleteFile(@ToolParameter(description = """
            Payload is path you need to delete
            """) String path) throws IOException {
        File f = new File(path);
        boolean isDelete = f.delete();
        return isDelete ? "Deleted file at path: " + path : " Not Deleted file" +
                " at path" + path;
    }

    @Tool(name = "MVN_CLEAN_INSTALL", description = """
            Run mvn clean install to check code builds with tests
            """)
    public String mvnCleanInstall(@ToolParameter(description = """
            Path of root project folder
            """) String path) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        String mvnCommand = os.contains("win") ? "mvn.cmd" : "mvn";
        ProcessBuilder pb = new ProcessBuilder(mvnCommand, "clean install");
        pb.directory(new java.io.File(path));
        pb.redirectErrorStream(true);
        StringBuilder output = new StringBuilder();
        try {
            Process process = pb.start();

            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            output.append("\nEXIT_CODE=").append(exitCode);
            return output.toString();
        } catch (Exception e) {
            return "Exception thrown from mvn compile";
        }
    }

    private boolean isRelevantFile(Path path) {
        String value = path.toString();
        return value.endsWith(".java")
                || value.endsWith(".xml")
                || value.endsWith(".yml")
                || value.endsWith(".yaml")
                || value.endsWith(".properties")
                || value.endsWith(".md");
    }

    private static void createDirectory(String filePath) {
        Path path = Paths.get(filePath);
        List<String> possiblePaths = new LinkedList<>();
        while (path.getParent() != null) {
            possiblePaths.add(path.toString());
            path = path.getParent();
        }

        for (int i = possiblePaths.size() - 1; i >=1 ; i--) {
            File tmp = new File(possiblePaths.get(i));
            if (!tmp.exists()) {
                tmp.mkdir();
            }
        }
    }

}
