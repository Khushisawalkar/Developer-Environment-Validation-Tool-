package com.nexus.model;

public record SystemProcess(long pid, String name, String user, String commandLine) {}
