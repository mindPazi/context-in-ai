# Context in AI Tool

![Build](https://github.com/mindPazi/context-in-ai-tool/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

IntelliJ IDEA plugin that extracts all Java methods from your project and exports them to a structured JSON file.

<!-- Plugin description -->
Extract all methods, anonymous classes, and lambda expressions from your Java project into a single JSON file. Useful for code analysis, documentation generation, and providing context to AI tools.

Features:

- Extracts all methods from Java files in your project
- Includes anonymous classes and lambda expressions
- Generates a `methods.json` file in your project root
- Shows extraction statistics (methods, anonymous classes, lambdas)
- File-based indexing for fast access
<!-- Plugin description end -->

## Usage

1. Open any Java project in IntelliJ IDEA
2. Go to **Tools** > **Dump Methods to JSON**
3. Wait for indexing to complete (if needed)
4. Find the generated `methods.json` file in your project root

The JSON file contains:

```json
[
  {
    "classFqn": "com.example.MyClass",
    "methodName": "myMethod",
    "signature": "(String, int)",
    "filePath": "/path/to/MyClass.java",
    "body": "{ ... }"
  }
]
```

## Installation

### From IDE

<kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "context-in-ai-tool"</kbd> > <kbd>Install</kbd>

### From Disk

Download the [latest release](https://github.com/mindPazi/context-in-ai-tool/releases/latest) and install manually:

<kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Building from Source

```bash
./gradlew clean build
```

The plugin will be available in `build/distributions/`.

## Requirements

- IntelliJ IDEA 2024.3+
- Java project support

## License

See [LICENSE](LICENSE)

---
Plugin based on the [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template).
