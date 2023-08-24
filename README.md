# Brinvex-Util-Amundi

## Introduction

_Brinvex-Util-Amundi_ is a compact Java library which enables developers 
to easily extract and work with data from Amundi reports.

## How to use it
 
- Add dependencies
````
<repository>
    <id>brinvex-mvnrepo</id>
    <url>https://github.com/brinvex/brinvex-mvnrepo/raw/main/</url>
    <snapshots>
        <enabled>true</enabled>
        <updatePolicy>always</updatePolicy>
    </snapshots>
</repository>

<dependency>
    <groupId>com.brinvex.util</groupId>
    <artifactId>brinvex-util-amundi-api</artifactId>
    <version>1.1.0</version>
</dependency>
<dependency>
    <groupId>com.brinvex.util</groupId>
    <artifactId>brinvex-util-amundi-impl</artifactId>
    <version>1.1.0</version>
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
