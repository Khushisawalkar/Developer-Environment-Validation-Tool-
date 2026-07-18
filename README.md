# 🚀 Developer Environment Validation Tool

A desktop application that validates a developer's workstation by automatically verifying software dependencies, system configuration, and available system resources before development begins. The tool helps simplify environment setup, identify configuration issues, and improve developer productivity across Windows and Linux.

## ✨ Features

### 🔍 Dependency Validation
Automatically detects and verifies essential development tools including Java, Git, Python, Maven, and Node.js.

### ⚙️ Environment Configuration
Validates environment variables, system configuration, and project prerequisites required for development.

### 💻 System Resource Monitoring
Displays CPU, memory, and disk availability to identify resource limitations before running applications.

### 📊 Readiness Report
Generates a system readiness report highlighting missing dependencies, configuration issues, and recommended fixes.

### 📁 Advanced Workspace Management & Tracking (New)
- **Automatic Project Detection**: Intelligently identifies project types (Maven, Gradle, npm) and primary languages dynamically.
- **Git Integration**: Asynchronously fetches real-time Git status (current branch, clean/uncommitted changes) using Java `ProcessBuilder`.
- **Smart Sorting**: Tracks your recently opened projects and automatically sorts your workspace list for quick access.
- **Project Statistics**: Analyzes your workspace to surface key dependencies (e.g., PostgreSQL, Spring Boot, React) and last modified timestamps.

## 🛠️ Tech Stack

- Java 17
- JavaFX
- Git
- Linux / Windows
- Java ProcessBuilder API

## 📂 Project Structure

```
Developer-Environment-Validation-Tool/
│
├── src/
├── docs/
├── screenshots/
├── README.md
└── pom.xml
```

## 📖 Documentation

- **[User Guide](./docs/user-guide.md)**: Instructions on how to run the application, prerequisites, and troubleshooting.
- **[Technical Documentation](./docs/technical-documentation.md)**: Architectural overview, validation logic, and extension guidelines.

## 🚀 Future Improvements

- Docker environment validation
- WSL support
- Automatic dependency installation
- PDF report generation
- Project-specific validation profiles

## 📚 Learning Outcomes

- Java Desktop Development
- Operating System Integration
- Process Management
- Environment Configuration
- System Automation
