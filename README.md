# 🌸 Nexus - Cozy Developer Workspace Manager

A beautiful, pastel-themed "cozy" Developer Workspace Manager built with **Java 17, Spring Boot, and JavaFX**. Nexus helps you manage your development environments, monitor system load, and quickly jump into your coding projects without the hassle.

## ✨ Features

### 🏡 Context-Aware Environment Scanner
Instead of blindly checking your system, Nexus parses your active project's dependencies (like `pom.xml` or `package.json`). It automatically detects whether you need Java, Node.js, Docker, or Postgres, and dynamically scans your system (and even WSL!) to make sure you have everything installed. Missing a tool? Nexus provides a convenient one-click download link.

### 📁 Workspace Profiles
Save and serialize your workspace configurations. With a single click, Nexus can launch your IDE (like IntelliJ or VSCode) and spin up a terminal perfectly mapped to your project directory. 

### 💻 Process Manager
A native OS process manager built directly into the UI. View all active processes, memory/CPU usage, and forcefully terminate rogue tasks directly from Nexus. 

### 🌿 System Health Monitor & Optimizer
A background OS poller continuously monitors your CPU and RAM usage. If your system load spikes above 85%, the Health Widget flashes a warning and reveals a **Refresh System** button. Clicking it safely flushes your DNS, clears bloated Windows `%TEMP%` files, and forces JVM Garbage Collection.

## 🛠️ Tech Stack
* **Language**: Java 17
* **Framework**: Spring Boot
* **UI**: JavaFX (with a custom CSS pastel/scrapbook design)
* **OS Interop**: Native Java `ProcessBuilder`, `ProcessHandle`, and `OperatingSystemMXBean`

## 🚀 Getting Started

1. Ensure you have **Java 17** installed.
2. Clone the repository.
3. Import the project into IntelliJ IDEA as a Maven project.
4. Run `NexusApplication.main()`!

## 💌 Design Philosophy
Focus on progress, not perfection. Nexus embraces a "cozy coding" aesthetic with soft pastel colors, rounded typography, and cute ascii graphics, proving that powerful developer tooling doesn't *have* to look like a hacker terminal.
