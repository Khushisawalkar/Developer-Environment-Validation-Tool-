package com.nexus.model;

import java.util.List;

public class WorkspaceProfile {
    private String id;
    private String name;
    private String projectPath;
    private String ideCommand;
    private List<String> browserUrls;
    
    // New fields for advanced tracking
    private String projectType;
    private String gitBranch;
    private String gitStatus;
    private long lastOpened;
    private String primaryLanguage;
    private long lastModified;
    private List<String> keyDependencies;

    // Default constructor for Jackson
    public WorkspaceProfile() {}

    public WorkspaceProfile(String id, String name, String projectPath, String ideCommand, List<String> browserUrls) {
        this.id = id;
        this.name = name;
        this.projectPath = projectPath;
        this.ideCommand = ideCommand;
        this.browserUrls = browserUrls;
        this.lastOpened = System.currentTimeMillis();
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
    
    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
    
    public String getGitBranch() { return gitBranch; }
    public void setGitBranch(String gitBranch) { this.gitBranch = gitBranch; }
    
    public String getGitStatus() { return gitStatus; }
    public void setGitStatus(String gitStatus) { this.gitStatus = gitStatus; }
    
    public long getLastOpened() { return lastOpened; }
    public void setLastOpened(long lastOpened) { this.lastOpened = lastOpened; }
    
    public String getPrimaryLanguage() { return primaryLanguage; }
    public void setPrimaryLanguage(String primaryLanguage) { this.primaryLanguage = primaryLanguage; }
    
    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }
    
    public List<String> getKeyDependencies() { return keyDependencies; }
    public void setKeyDependencies(List<String> keyDependencies) { this.keyDependencies = keyDependencies; }
}
