# JanControl

Prototype to replace [fancontrol](https://github.com/lm-sensors/lm-sensors/blob/master/doc/fancontrol.txt) with a tool that supports a more convenient config DSL.

It also supports more than one input per fan.

## Build

```bash
mvn -U -DskipTests package
```

## Run

```bash
java -jar jancontrol-0.0.1-SNAPSHOT.jar <config_file>
```

## Note on chosen technology

Arguably Java and Spring Boot are a bad choice for such a tool. However, this has been chosen because this tool serves as a training project. Think of it as a Java/SpringBoot Kata.