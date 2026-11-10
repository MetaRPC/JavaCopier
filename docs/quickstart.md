# First request with Java

List your own copiers to check connectivity and identity without creating a terminal or placing a trade.

## 1. Install

Requires **JDK 11+ and Maven 3.8+**. These instructions build from source; they do not assume a registry release exists.

```bash
git clone https://github.com/MetaRPC/JavaCopier.git
cd JavaCopier
mvn test
mvn dependency:copy-dependencies
javac -cp "target/classes" -d target/examples examples/ListCopiers.java
```

Java on Windows uses `;` for classpath separators. Run C++ builds in a Visual Studio developer shell. Activate your Python virtual environment before installing.

## 2. Set your owner key

=== "Bash"
    ```bash
    export MRPC_API_KEY='your-owner-key'
    ```
=== "PowerShell"
    ```powershell
    $env:MRPC_API_KEY = 'your-owner-key'
    ```

Use the real owner API key, not a broker password, terminal ID or `TRIAL`. Read [authentication](authentication.md) if access is refused.

## 3. Run

```bash
java -cp "target/examples:target/classes:target/dependency/*" ListCopiers
```

Source: [`examples/ListCopiers.java`](https://github.com/MetaRPC/JavaCopier/blob/main/examples/ListCopiers.java).

```java
import pro.mrpc.copier.CopierService;
import pro.mrpc.copier.Models;
public class ListCopiers {
    public static void main(String[] args) {
        try (CopierService service = new CopierService("https://copy.mrpc.pro", System.getenv("MRPC_API_KEY"))) {
            Models.ListReply result = service.list();
            if (!result.ok) throw new IllegalStateException(result.error);
            for (Models.CopierSummary item : result.copiers) System.out.println(item.id + ": paused=" + item.paused);
        }
    }
}
```

A successful empty list means this owner has no copiers. Treat failed requests as errors, not empty accounts.

## 4. Prepare a demo workflow

Read [the lifecycle guide](lifecycle.md), then [choose sizing](sizing.md). Existing replication examples under `examples/` are advanced integration examples: they may provision accounts and trade. Inspect them and their cleanup before running with disposable demos. List is the default starting point.
