# Developer Environment Validation Tool - User Guide

## Introduction
Welcome to the Developer Environment Validation Tool! This application eliminates the "it works on my machine" problem by ensuring your local workstation has all the necessary dependencies, configurations, and system resources before you begin development.

## Getting Started

### Prerequisites
- Java 17 or higher installed on your machine.
- Minimum 2GB of available RAM.

### Setup and Project Configuration
1. **Launch the tool** by running the generated `.jar` file or executing it through your IDE.
2. **Select a Workspace:** Upon launching, you will be prompted to select a local project directory. 
3. **Project Configuration:** The application will automatically parse your workspace (e.g., scanning `pom.xml` or `package.json`) to configure the necessary validation rules specific to your project. No manual rule configuration is required!

## Understanding the Validation Process

Once a project is selected, the tool automatically performs the following checks:

### 1. Project Analysis
The tool scans your project files (e.g., `pom.xml`, `package.json`) to intelligently determine which software stacks are required for this specific project.

### 2. Dependency Validation
The scanner will check your system for required tools such as:
- Java, Maven, Node.js
- Git
- Databases (MySQL, PostgreSQL, Redis)
- Docker

*Note: The tool checks both your native Windows environment and Windows Subsystem for Linux (WSL).*

### 3. System Health Check
The application verifies that your machine has adequate system resources (CPU availability, free RAM, disk space) to run the project smoothly without crashing.

## Reviewing the Results
Once the scan is complete, a visual **Readiness Report** is generated:
- ✅ **Green / Success:** The dependency is installed and functioning correctly.
- ❌ **Red / Missing:** The dependency could not be found. 

### Fixing Missing Dependencies
If a required tool is missing, the Readiness Report will provide a direct **Download Link** to the official installation page for that specific software. 
1. Click the provided link.
2. Install the software following the official instructions.
3. Restart your terminal/IDE if necessary, and re-run the validation tool to confirm the installation.

## Troubleshooting
- **Tool installed but showing as missing?** Ensure the software is added to your system's `PATH` environment variable. If using WSL, ensure the WSL instance is running and the tool is installed within the Linux environment.
- **Timeout errors?** Some commands may take longer to execute on slower machines. Ensure no background processes are locking the execution.
