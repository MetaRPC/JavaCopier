# JavaCopier

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Docs](https://img.shields.io/badge/docs-online-green.svg)](https://github.com/MetaRPC/JavaCopier/tree/main/docs)

Official Java SDK for the MetaRPC Trade Copier high-performance trade replication engine via gRPC (`copy.mrpc.pro:443`).

## Installation

```bash
implementation 'pro.mrpc:copier:1.0.0'
```

---

## 🏃 How to Run Examples

### 1. Clone Repo
```bash
git clone https://github.com/MetaRPC/JavaCopier.git
cd JavaCopier
```

### 2. Run with Default TRIAL Key
```bash
# Using helper script on Windows:
.\run.bat

# Or directly with javac/java (Linux / macOS / Windows):
javac -d bin $(find src/main/java examples -name "*.java")
java -cp bin QuickStart
```

### 3. Run with Your Own API Key

Pass your API key directly as an argument:
```bash
# Windows
.\run.bat <YOUR_API_KEY>

# Cross-platform
java -cp bin QuickStart <YOUR_API_KEY>
```

Or set the `MRPC_API_KEY` environment variable:
```bash
# Linux / macOS
export MRPC_API_KEY="<YOUR_API_KEY>"
java -cp bin QuickStart

# Windows PowerShell
$env:MRPC_API_KEY="<YOUR_API_KEY>"
.\run.bat

# Windows CMD
set MRPC_API_KEY=<YOUR_API_KEY>
run.bat
```

---

## Quick Start

See [Quick Start Documentation](https://github.com/MetaRPC/JavaCopier/blob/main/docs/All_Guides/Your_First_Project.md) for a 10-minute walkthrough.

