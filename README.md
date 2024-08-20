## Brinvex-Util-Amundi

### Introduction

_Brinvex-Util-Amundi_ is a compact Java library which enables developers 
to easily extract and work with data from Amundi reports.

### Maven dependency
 
````
<properties>
     <brinvex-util-amundi.version>1.4.1</brinvex-util-amundi.version>
</properties>

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
    <version>${brinvex-util-amundi.version}</version>
</dependency>
<dependency>
    <groupId>com.brinvex.util</groupId>
    <artifactId>brinvex-util-amundi-impl</artifactId>
    <version>${brinvex-util-amundi.version}</version>
    <scope>runtime</scope>
</dependency>
````
### Example
````
AmndParser parser = AmundiService.create();
parser.parseTransactionStatements(...); 
````
### Recommendations
If using within Spring ecosystem, consider to exclude ```commons-logging```.

### Requirements
- Java 21 or above

### License

- The _Brinvex-Util-Amundi_ is released under version 2.0 of the Apache License.
