# Developer Environment Validation Tool - Technical Documentation

## 1. Architecture Overview
The Developer Environment Validation Tool is a JavaFX-based desktop application designed to ensure that developers' workstations meet the required prerequisites for any given project. It integrates seamlessly with the host operating system to execute validation commands across multiple environments (native Windows and WSL).

### Core Components
- **EnvironmentScannerService**: The core scanning engine that executes system commands to check for the presence and version of essential software tools.
- **ProjectAnalyzerService**: Analyzes the target project (e.g., parsing `pom.xml` or `package.json`) to dynamically determine which tools actually need to be validated.
- **ProcessManagerService**: Handles the secure execution and lifecycle management of background processes.
- **SystemHealthService**: Monitors CPU, RAM, and Disk space availability to prevent out-of-resource scenarios during local development.
- **WorkspaceManagerService**: Handles workspace initialization and configuration checks.

---

## 2. Validation Engine Details

### Supported Software
The scanner currently identifies the following software stacks and provides automated download URLs if missing:
- **Core Languages & Runtimes:** Java, Node.js
- **Build Tools:** Maven
- **Version Control:** Git
- **Containerization:** Docker
- **Databases & Caches:** PostgreSQL, MySQL, Redis

### Execution Strategy
The `EnvironmentScannerService` employs a robust 3-tier fallback strategy to accurately detect installed tools:
1. **Primary Check (Native PATH):** Executes the standard version command (e.g., `java -version`) using `cmd.exe`.
2. **Fallback Check (Default Install Paths):** If not found in the system PATH, the tool searches common default installation directories (e.g., `C:\Program Files\MySQL\MySQL Server X.Y\bin`).
3. **Subsystem Check (WSL):** If native checks fail, it falls back to the Windows Subsystem for Linux (WSL) by prepending `wsl` to the command (e.g., `wsl mysql -V`), ensuring maximum compatibility for developers utilizing a hybrid OS environment.

---

## 3. Asynchronous Execution
To ensure the UI remains responsive, the application leverages `CompletableFuture` to run environment scans in parallel. 

- The `scanEnvironment` method first resolves the required tools via the `ProjectAnalyzerService`.
- Tasks are generated and executed on a background thread pool.
- Each process execution is wrapped with an enforced timeout (`process.waitFor(2, TimeUnit.SECONDS)`) to prevent hanging commands from freezing the validation pipeline.

---

## 4. Extension Guidelines
To add support for a new software dependency:
1. Open `EnvironmentScannerService.java`.
2. Add the tool to the `getCheckForTool` switch statement with its respective version command.
3. Add the official download link to the `getDownloadUrl` method.
4. If the tool is commonly installed outside the standard PATH, add a fallback path handler in `getFallbackCommand`.
