package com.nexus.model;

import java.util.List;

public class WorkspaceProfile {
    private String id;
    private String name;
    private String projectPath;
    private String ideCommand;
    private List<String> browserUrls;

    // Default constructor for Jackson
    public WorkspaceProfile() {}

    public WorkspaceProfile(String id, String name, String projectPath, String ideCommand, List<String> browserUrls) {
        this.id = id;
        this.name = name;
        this.projectPath = projectPath;
        this.ideCommand = ideCommand;
        this.browserUrls = browserUrls;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProjectPath() { return projectPath; }
    public void setProjectPath(String projectPath) { this.projectPath = projectPath; }
    public String getIdeCommand() { return ideCommand; }
    public void setIdeCommand(String ideCommand) { this.ideCommand = ideCommand; }
    public List<String> getBrowserUrls() { return browserUrls; }
    public void setBrowserUrls(List<String> browserUrls) { this.browserUrls = browserUrls; }
}
