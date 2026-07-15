# JanControl

Prototype to replace [fancontrol](https://github.com/lm-sensors/lm-sensors/blob/master/doc/fancontrol.txt) with a tool
that supports a more powerful and convenient config DSL.

It also supports more than one input per fan.

**WARNING:** Use at your own risk! The app may damage things, because it writes directly to device files in /sys. You
have been warned!

## Home

[github.com/gigabitzauber/jancontrol](https://github.com/gigabitzauber/jancontrol) - [gigabitzauber.de](https://gigabitzauber.de)

## Build

The sources come with [Maven Wrapper](https://maven.apache.org/tools/wrapper) attached. If you don't have Maven
installed, you can use the wrapper to build the project.

Linux/Mac:

```bash
./mvnw -U clean package
```

Windows:

```bash
mvnw.cmd -U clean package
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
java -jar jancontrol-0.2.1-SNAPSHOT.jar <config_file>
```

## Debug mode / Verbose output

The command line switch `-v` activates verbose output.

```bash
java -jar jancontrol-0.2.1-SNAPSHOT.jar <config_file> -v
```

## How it works

See [docs/how_to_use.md](docs/how_to_use.md) for details on how configuration works.

## Examples

See `docs/examples` for example configuration files.

## Note on chosen technology

Arguably Java and Spring Boot are a bad choice for such a tool. However, this has been chosen because this tool serves
as a training project. Think of it as a Java/SpringBoot Kata.
