# JanControl

A replacement for [fancontrol](https://github.com/lm-sensors/lm-sensors/blob/master/doc/fancontrol.txt) that supports a
convenient and more powerful config DSL.

It also supports more than one input per fan.

**WARNING:** Make sure, you are using proper values in the config! There are basic validations in place, but it is not
yet foolproof. In particular curve integrity is currently not checked at all. You have been warned!

## Table of Contents

<!--ts-->

* [Home](#home)
* [Build](#build)
* [Run](#run)
    * [Debug mode / Verbose output](#debug-mode--verbose-output)
* [Configure](#configure)
* [Examples](#examples)
* [Does it survive Suspend and Hibernation?](#does-it-survive-suspend-and-hibernation)
* [Note on chosen technology](#note-on-chosen-technology)

<!--te-->

<small>TOC creation kindly provided
by [github-markdown-toc](https://github.com/ekalinin/github-markdown-toc#installation).</small>

## Home

[github.com/gigabitzauber/jancontrol](https://github.com/gigabitzauber/jancontrol) - [gigabitzauber.de](https://gigabitzauber.de) - [Support me](https://ko-fi.com/gigabitzauber)

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
java -jar jancontrol-0.4.0-SNAPSHOT.jar [options] <config-file>
```

## Install

```bash
# -f.. Fails silently (no output) on server errors (HTTP 4xx or 5xx) instead of outputting the error page.
# -s.. Silent mode. Prevents curl from showing a progress meter or error messages.
# -S.. --show-error	When used with -s, this forces curl to output an error message if it fails.
# -L.. --location Tells curl to follow redirects if the server reports that the requested page has moved to a different location.
curl -fsSL https://raw.githubusercontent.com/gigabitzauber/jancontrol/refs/heads/main/scripts/install.sh | sudo bash
```

### Debug mode / Verbose output

The command line switch `-v` activates verbose output.

```bash
java -jar jancontrol-0.4.0-SNAPSHOT.jar -v <config-file>
```

## Configure

See [docs/how_to_use.md](docs/how_to_use.md) for details on how configuration works.

## Examples

See folder [docs/examples](docs/examples) for example configuration files.

## Does it survive Suspend and Hibernation?

Yes, since v0.3.0. Suspend / Hibernate usually puts fans back into full auto mode and the tool will recognize this and
enforce its config.

## Note on chosen technology

Arguably Java and Spring Boot are a bad choice for such a tool. However, this has been chosen because this tool serves
as a training project. Think of it as a Java/SpringBoot Kata.
