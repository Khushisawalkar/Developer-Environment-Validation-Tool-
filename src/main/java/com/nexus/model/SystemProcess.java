package com.nexus.model;

public record SystemProcess(
    long pid, 
    String name, 
    String user, 
    String commandLine, 
    long memoryBytes, 
    String memoryStr, 
    String category, 
    String safeness,
    boolean isHung,
    String uptimeStr
) {}
