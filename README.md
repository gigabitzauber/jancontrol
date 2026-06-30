# JanControl

Prototype to replace [fancontrol](https://github.com/lm-sensors/lm-sensors/blob/master/doc/fancontrol.txt) with a tool
that supports a more convenient config DSL.

It also supports more than one input per fan.

*WARNING:* Use at your own risk! This is an alpha prototype. It may damage things, because it writes directly to device
files in /sys. You have been warned!

## Build

```bash
mvn -U -DskipTests package
```

Please note that if you get a warning like this:

```
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by lombok.permit.Permit
WARNING: Please consider reporting this to the maintainers of class lombok.permit.Permit
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
```

You'll need to add "--sun-misc-unsafe-memory-access=allow" to your MAVEN_OPTS.
See [Lombok Issues](https://github.com/projectlombok/lombok/issues/3852#issuecomment-3114204225) for details.

## Run

```bash
java -jar jancontrol-0.0.1-SNAPSHOT.jar <config_file>
```

## Note on chosen technology

Arguably Java and Spring Boot are a bad choice for such a tool. However, this has been chosen because this tool serves
as a training project. Think of it as a Java/SpringBoot Kata.