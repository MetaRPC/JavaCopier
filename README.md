# MetaRPC Java Trade Copier SDK

Authenticate, configure a master–follower relationship, inspect its state, pause and remove it.

**[Documentation](https://metarpc.github.io/JavaCopier/) · [Quickstart](https://metarpc.github.io/JavaCopier/quickstart/) · [Authentication](https://metarpc.github.io/JavaCopier/authentication/) · [API reference](https://metarpc.github.io/JavaCopier/reference/)**

## Install from source

Requires JDK 11+ and Maven 3.8+.

```bash
git clone https://github.com/MetaRPC/JavaCopier.git
cd JavaCopier
mvn test
mvn dependency:copy-dependencies
javac -cp "target/classes" -d target/examples examples/ListCopiers.java
```

## First request

Set `MRPC_API_KEY` to your real owner API key in your environment, then run:

```bash
java -cp "target/examples:target/classes:target/dependency/*" ListCopiers
```

This List example is read-only: no account creation or trades. Java on Windows uses `;` instead of `:` in classpaths.

## Authentication

Every SDK call sends `ApiKey` (`apikey` in gRPC metadata). The gRPC body also carries `user_key`; the header does not replace it. `TRIAL` is not an owner key. Read the [verified transport contract](https://metarpc.github.io/JavaCopier/authentication/).

## Scope and verification

Core operations: start · list · pause · remove. Advanced server features are distinguished from the supported wrapper surface. Review legacy replication examples before using them with disposable demos. Standard tests need no broker credentials.

See [testing and contributing](https://metarpc.github.io/JavaCopier/testing/) for the regression suite, demo checklist and Pages build. Edit `docs/`; generated `site/` output is not source.
