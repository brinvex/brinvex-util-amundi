set JAVA_HOME="C:\tools\java\jdk-21.0.1"

set new_version=1.3.0

set jsh_content=^
    Files.writeString(Path.of("README.md"), ^
        Files.readString(Path.of("README.md")).replaceAll(^
            "<brinvex-util-amundi.version>(.*)</brinvex-util-amundi.version>", ^
            "<brinvex-util-amundi.version>%%s</brinvex-util-amundi.version>".formatted(System.getenv("new_version"))), ^
    StandardOpenOption.TRUNCATE_EXISTING);

echo %jsh_content% | %JAVA_HOME%\bin\jshell -

call mvnw clean -Darguments=-DskipTests license:format
call mvnw clean package
call mvnw versions:set -DnewVersion=%new_version%
call mvnw versions:commit
call mvnw clean deploy -DskipTests