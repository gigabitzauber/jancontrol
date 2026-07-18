# JanControl

A replacement for [fancontrol](https://github.com/lm-sensors/lm-sensors/blob/master/doc/fancontrol.txt) with an app that
supports a more convenient and powerful config DSL.

It also supports more than one input per fan.

**WARNING:** Make sure, you are using proper values in the config! There are basic validations in place, but it is not
yet foolproof. In particular curve integrity is currently not checked at all. You have been warned!

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
java -jar jancontrol-0.3.0.jar [options] <config-file>
```

### Debug mode / Verbose output

The command line switch `-v` activates verbose output.

```bash
java -jar jancontrol-0.3.0.jar -v <config-file>
```

## Configure

See [docs/how_to_use.md](docs/how_to_use.md) for details on how configuration works.

## Examples

See `docs/examples` for example configuration files.

## Note on chosen technology

Arguably Java and Spring Boot are a bad choice for such a tool. However, this has been chosen because this tool serves
as a training project. Think of it as a Java/SpringBoot Kata.
