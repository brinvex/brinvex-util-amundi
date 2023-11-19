# Brinvex-Util-Amundi

## Introduction

_Brinvex-Util-Amundi_ is a compact Java library which enables developers 
to easily extract and work with data from Amundi reports.

## How to use it
 
- Add dependencies
````
<repository>
    <id>repository.brinvex</id>
    <name>Brinvex Repository</name>
    <url>https://github.com/brinvex/brinvex-repo/raw/main/</url>
    <snapshots>
        <enabled>false</enabled>
    </snapshots>
</repository>

<dependency>
    <groupId>com.brinvex.util</groupId>
    <artifactId>brinvex-util-amundi-api</artifactId>
    <version>1.2.0</version>
</dependency>
<dependency>
    <groupId>com.brinvex.util</groupId>
    <artifactId>brinvex-util-amundi-impl</artifactId>
    <version>1.2.0</version>
    <scope>runtime</scope>
</dependency>
````
- Process statements files 
````
AmundiService amundiSvc = AmundiServiceFactory.INSTANCE.getService();
amundiSvc.parseTransactionStatements(...); 
````

### Requirements
- Java 17 or above

### License

- The _Brinvex-Util-Amundi_ is released under version 2.0 of the Apache License.
