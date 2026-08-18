# JanControl

An alternative for [fancontrol](https://github.com/lm-sensors/lm-sensors/blob/master/doc/fancontrol.txt) that supports a
convenient and more powerful config DSL.

It also supports more than one input per fan and does also work on Thinkapds, making it a possible alternative
for [thinkfan](https://github.com/vmatare/thinkfan) as well.

**WARNING:** Make sure, you are using proper values in the config! There are basic validations in place, but it is not
yet foolproof. In particular curve integrity is currently not checked at all. You have been warned!

[Consider supporting if you find this useful](https://ko-fi.com/gigabitzauber)

## Table of Contents

<!--ts-->

* [Home](#home)
* [Build](#build)
* [Run](#run)
    * [Command Line Switches](#command-line-switches)
* [Install](#install)
* [Running as Service (systemd)](#running-as-service-systemd)
* [Configure](#configure)
* [Examples](#examples)
* [Supported Hardware](#supported-hardware)
* [FAQ](#faq)
    * [Does it survive Suspend and Hibernation?](#does-it-survive-suspend-and-hibernation)
    * [My fans won't stop even though I configured rpm: 0](#my-fans-wont-stop-even-though-i-configured-rpm-0)
    * [Fan activation threshold](#fan-activation-threshold)
    * [Is there a debug mode / verbose output?](#is-there-a-debug-mode--verbose-output)
    * [Does it recognize config file changes?](#does-it-recognize-config-file-changes)
    * [Does it work on Thinkpads?](#does-it-work-on-thinkpads)
    * [My temperature sensor file is not found anymore](#my-temperature-sensor-file-is-not-found-anymore)
* [Note on chosen technology](#note-on-chosen-technology)

<!--te-->

<small>TOC creation kindly provided
by [github-markdown-toc](https://github.com/ekalinin/github-markdown-toc#installation).</small>

## Home

[github.com/gigabitzauber/jancontrol](https://github.com/gigabitzauber/jancontrol) - [gigabitzauber.de](https://gigabitzauber.de) - [Support me](https://ko-fi.com/gigabitzauber)

## Build

The sources come with [Maven Wrapper](https://maven.apache.org/tools/wrapper) attached. If you don't have Maven
installed, you can use the wrapper to build the project.

```bash
./mvnw -U clean package
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
java -jar jancontrol-0.6.4.jar [options] <config-file>
```

### Command Line Switches

```bash
Usage: java -jar jancontrol.jar [options] <config-file>

Options:
-h | --help ... show this help
-w ... watch config file for changes
-v ... activate verbose mode
--version ... show version
```

## Install

* -f.. Fails silently (no output) on server errors (HTTP 4xx or 5xx) instead of outputting the error page.
* -s.. Silent mode. Prevents curl from showing a progress meter or error messages.
* -S.. --show-error When used with -s, this forces curl to output an error message if it fails.
* -L.. --location Tells curl to follow redirects if the server reports that the requested page has moved to a different
  location.

```bash
curl -fsSL https://raw.githubusercontent.com/gigabitzauber/jancontrol/refs/heads/main/scripts/install.sh | sudo bash
```

## Running as Service (systemd)

Either use the install-script or see [scripts/jancontrol.service](scripts/jancontrol.service) for a working systemd unit
file.

## Configure

See [docs/how_to_use.md](docs/how_to_use.md) for details on how configuration works.

## Examples

See folder [docs/examples](docs/examples) for example configuration files.

## Supported Hardware

Basically all temperature sensors / RPM-controllable devices are supported as long as there is a
[HWMON driver](https://docs.kernel.org/hwmon/hwmon-kernel-api.html) available.

However, the devil is in the details. Long story short: See [docs/how_to_use.md](docs/how_to_use.md) for details on
supported hardware and how to configure.

## FAQ

### Does it survive Suspend and Hibernation?

Yes, since v0.3.0. Suspend / Hibernate usually puts fans back into full auto mode and the tool will recognize this and
enforce its config.

### My fans won't stop even though I configured rpm: 0

By default, the tool will not allow fans to spin at less than 20% to make sure hardware is not overheating. However,
there are cases where this is still desirable, e.g. on laptops to make them as quiet as possible. Thus, this safety
mechanism can be overridden like this:

```yaml
fans:
  - interval: "3s"
    device:
      allowIdle: true
...
```

### Fan activation threshold

Most fans do not activate when configured to spin at e.g. 1% rpm. They have an individual threshold at which they
activate. This is called the activation threshold. To not bother the hardware with values below this threshold, the tool
will set rpm to 0% for all configured curve values below this threshold.

The default threshold is 20%.

However, you may override this threshold like this:

```yaml
fans:
  - interval: "3s"
    device:
      activationThreshold: 15
...
```

### Is there a debug mode / verbose output?

The command line switch `-v` activates verbose output.

```bash
java -jar jancontrol-0.6.4.jar -v <config-file>
```

### Does it recognize config file changes?

Usually the tool must be restarted to recognize config file changes. However, with the switch `-w` it is possible to
start it in "watch mode", i.e. it will recognize config file changes and reload its configuration automatically.

```bash
java -jar jancontrol-0.6.4.jar -w <config-file>
```

### Does it work on Thinkpads?

Yes, currently Thinkpad fan control is supported via
the [thinkpad_acpi](https://docs.kernel.org/admin-guide/laptops/thinkpad-acpi.html) Kernel module.

Please note that you may need to set `fan_control=1`as a module parameter to unlock manual fan control. However, this
depends on your Thinkpad model.

See [docs/how_to_use.md](docs/how_to_use.md) on how to configure Thinkpad support.

### My temperature sensor file is not found anymore

This may happen after a Kernel upgrade. In this case, hwmon numbering may be changed. Try to find out the new number.

## Note on chosen technology

Arguably Java and Spring Boot are a bad choice for such a tool. However, this has been chosen because this tool serves
as a training project. Think of it as a Java/SpringBoot Kata.
