# How to use jancontrol

## Table of Contents

<!--ts-->

* [How devices are identified](#how-devices-are-identified)
* [Config file reference](#config-file-reference)
* [Supported Hardware](#supported-hardware)
* [How do multiple temperature sensors for one fan work?](#how-do-multiple-temperature-sensors-for-one-fan-work)
* [How to prevent fans from continuously spinning up and down?](#how-to-prevent-fans-from-continuously-spinning-up-and-down)

<!--te-->

The tool tries to figure out how fast a fan should spin based on one or more temperature values it reads. This is
usually nothing more than "if temperature is X, then fan should spin Y RPMs". The mapping of temperature value to fan
speed is defined in the configuration file. This file contains a [YAML](https://en.wikipedia.org/wiki/YAML) based
description of one or more of such fans.

Basically it works like this:

1. Read temperature value from sensors that a fan depends on (called "dependencies").
2. Set the desired fan speed based on the curve that has been configured for this fan.
3. Repeat this process every few seconds (configurable per fan as "interval").

## How devices are identified

The tool uses the [HWMON subsystem](https://docs.kernel.org/hwmon) of the Linux kernel, i.e. it writes to device files
in directory `/sys/class/hwmon/hwmonN`. A special resolving mechanism is used to identify the directory for each device.
Each of these directories contains a file called `name`. It contains the name of the hwmon class (i.e. roughly the name
of the kernel module) which you must enter into the `sysName` key of the device's specification. The `slot` key however
contains the number of the device inside the hwmonN directory.

Because the `N` in `/sys/class/hwmon/hwmonN` may change after reboot, this mechanism is used instead of plainly
specifying a file's absolute path.

E.g. the contents of a hwmon dir may look like this:

| File        | Meaning                                       | Maps to device config key           |
|-------------|-----------------------------------------------|-------------------------------------|
| fan1_enable | N / A                                         | None                                |
| fan1_input  | N / A                                         | None                                |
| fan1_max    | N / A                                         | None                                |
| fan1_min    | N / A                                         | None                                |
| fan1_target | N / A                                         | None                                |
| ...         | ...                                           | ...                                 |
| name        | Contains hwmon class / kernel module name     | `device.sysName` / `dependsOn.name` |
| ...         | ...                                           | ...                                 |
| pwm1        | PWM control of fan in slot 1                  | `device.slot`                       |
| pwm1_enable | Fan mode of fan in slot 1                     |                                     |
| pwm1_max    | N / A                                         | None                                |
| pwm1_min    | N / A                                         | None                                |
| temp1_input | Temperature readings of temp sensor in slot 1 | `dependsOn.slot`                    |
| temp1_label | Name of the temp sensor                       | None                                |
| temp2_input | Temperature readings of temp sensor in slot 2 | `dependsOn.slot`                    |
| temp2_label | Name of the temp sensor                       | None                                |

## Config file reference

The config file may specify the following things:

| Key                          | Meaning                                                                                                                                                   |
|------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `interval`                   | Optional. The update interval of this fan. Time units such as `500ms` or `3s` must be used. Default: `5s`.                                                |
| `downSkip`                   | Optional. Throttle decreasing RPM by skipping downSkip iterations. Default: 0.                                                                            |
| `n`                          | Optional. Calculated RPM values of each curve will be rounded to multiples of this value. Default: 1                                                      |
| `device`                     | The target device to control.                                                                                                                             |
| `device.ref`                 | A human-readable unique designation for the fan. It may contain spaces and is used as a reference for the curves defined for this fan.                    |
| `device.sysName`             | The system's name of the hwmon kernel module controling this device, i.e. the contents of the file `/sys/class/hwmon/hwmonN/name`. Example: `nct6799`.    |
| `device.slot`                | The number of the device. Starts at 1. Used to resolve the correct device file, e.g. the value `2` may resolve to `/sys/class/hwmon/hwmon7/pwm2`.         |
| `device.allowIdle`           | Optional. Disable safety RPM margins, i.e. fans may be set to off (0% RPM). Default: false                                                                |
| `device.activationThreshold` | Optional. All configured values below this threshold will be interpreted as 0% RPM. Default: 20                                                           |
| `device.driver`              | Optional. Specifies the name of the driver to use for low level fan I/O. See [Supported Hardware](#supported-hardware) for viable names. Default: nct6775 |
| `dependsOn.ref`              | A human-readable unique designation for the fan. It may contain spaces and is used as a reference for the curves defined for this fan.                    |
| `dependsOn.sysName`          | The system's name of the hwmon kernel module controling this device, i.e. the contents of the file `/sys/class/hwmon/hwmonN/name`. Example: `nct6799`.    |
| `dependsOn.slot`             | The number of the device. Starts at 1. Used to resolve the correct device file, e.g. the value `2` may resolve to `/sys/class/hwmon/hwmon7/pwm2`.         |
| `curves`                     | A list of temperature-to-RPM mappings used to interpolate the desired fan speed. Only linear interpolation is currently supported.                        |
| `curves.ref`                 | The ref of the dependency sensor that this curve references. It must match one of the refs defined in `dependsOn`.                                        |
| `curves.points`              | A list of points that define the mapping between temperature and fan speed.                                                                               |
| `curves.points.temp`         | The temperature value. The unit can be °C or °F, as long as it is used consistently.                                                                      |
| `curves.points.rpm`          | The desired fan speed value. This is expressed as a percentage from `0` to `100` and is translated to absolute RPMs at runtime.                           |

For example, the following snippet defines a fan that is controlled from a CPU temperature sensor using a simple linear
curve:

```yaml
fans:
  - interval: "3s"
    device:
      ref: "CPU Fan"
      sysName: "asusec"
      slot: 2
    dependsOn:
      - ref: "CPU Temp"
        sysName: "nct6799"
        slot: 8
    curves:
      - ref: "CPU Temp"
        points:
          - temp: 46
            rpm: 20
          - temp: 60
            rpm: 33
          - temp: 78
            rpm: 75
          - temp: 95
            rpm: 95
```

## Supported Hardware

Below you'll find hardware configurations that are supposed to work.

If nothing else is specified, the nct6775-driver will be used by default. To specify a driver, just write its name into
the config file:

```yaml
fans:
  - interval: "3s"
    device:
      driver: "thinkpad_acpi"
```

| Hardware | Config-File-Value       | Driver                                                                          | Comment                                                                                                                                                                                                         |
|----------|-------------------------|---------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| NCT6775  | nct6775 (default value) | [nct6775](https://docs.kernel.org/hwmon/nct6775.html)                           | Should also work for NCT6775F, NCT6776F & W83627EHF.                                                                                                                                                            |
| Thinkpad | thinkpad_acpi           | [thinkpad_acpi](https://docs.kernel.org/admin-guide/laptops/thinkpad-acpi.html) | May work on all models that support this kernel module(?) Please note that you may need to set `fan_control=1`as a module parameter to unlock manual fan control. However, this depends on your Thinkpad model. |

## How do multiple temperature sensors for one fan work?

A fan has exactly one `device` and one or more `dependsOn` entries, which are called `dependencies`. Each `dependency`
may be referenced by at most one curve defined for it. If multiple dependencies are defined for a fan, the tool will
calculate the desired fan speed for each dependency and then use the **highest value as the final desired fan speed**.
This enables scenarios where the fan speed depends on what the machine is currently doing.

For example, this is a real world config file that controls the case fan of a PC. It is based on whether the CPU is
currently compiling (GPU / Case cold), or it currently is summer (case hot, CPU & GPU cold) or the machine is currently
used for gaming (CPU & GPU hot, case temperature rising):

```yaml
fans:
  - interval: "20s"
    device:
      ref: "CPU Fan"
      sysName: "nct6799"
      slot: "2"
    dependsOn:
      - ref: "CPU Temp"
        sysName: "asusec"
        slot: "2"
    curves:
      - ref: "CPU Temp"
        points:
          - temp: 46
            rpm: 20
          - temp: 60
            rpm: 33
          - temp: 78
            rpm: 75
          - temp: 81
            rpm: 81
          - temp: 95
            rpm: 95
  - interval: "10s"
    device:
      ref: "Case Fan"
      sysName: "nct6799"
      slot: "1"
    dependsOn:
      - ref: "Case Temp"
        sysName: "asusec"
        slot: "3"
      - ref: "GPU Temp"
        sysName: "amdgpu"
        slot: "1"
      - ref: "CPU Temp"
        sysName: "asusec"
        slot: "2"
    curves:
      - ref: "Case Temp"
        points:
          - temp: 38
            rpm: 25
          - temp: 41
            rpm: 35
          - temp: 42
            rpm: 35
          - temp: 46
            rpm: 66
          - temp: 49
            rpm: 75
          - temp: 52
            rpm: 80
          - temp: 60
            rpm: 95
      - ref: "GPU Temp"
        points:
          - temp: 48
            rpm: 25
          - temp: 69
            rpm: 66
          - temp: 73
            rpm: 75
          - temp: 80
            rpm: 90
      - ref: "CPU Temp"
        points:
          - temp: 46
            rpm: 20
          - temp: 75
            rpm: 50
          - temp: 84
            rpm: 50
          - temp: 89
            rpm: 60
```

## How to prevent fans from continuously spinning up and down?

Depending on your system, configured curves, outside temperatures and desired "silent" RPMs, fans may have the tendency
to "twitch", i.e. they spin up to increase cooling and immediately spin down, because the temperature of the device they
are cooling decreased by e.g. 1°. Due to the spin down, the temperature increases again which in turn causes the fan to
spin up and the cycle repeats itself.

To prevent this, various different options are available:

* You may want to improve your curve setup.
* Try experimenting with `interval`. Longer intervals may help to "miss" short-lived changes in temperature values. But
  will also slow down reaction to high temperature increases
* Try using 'downSkip'. This acts like a kind of [hysteresis](https://en.wikipedia.org/wiki/Hysteresis#Control_systems),
  i.e. fans run longer on higher RPMs before spinning down, maybe decreasing the temperature by more than 1°.
* Try using 'n'. Spin fans up and down by multiples of n only. This makes RPMs more coarse grained which also may
  increase cooling per time.

For example this config is using these kinds of fine-tuning:

```yaml
fans:
  - interval: "20s"
    downSkip: 1
    n: 3
    device:
      ref: "CPU Fan"
      sysName: "nct6799"
      slot: "2"
    dependsOn:
      - ref: "CPU Temp"
        sysName: "asusec"
        slot: "2"
    curves:
      - ref: "CPU Temp"
        points:
          - temp: 46
            rpm: 20
          - temp: 60
            rpm: 33
          - temp: 78
            rpm: 75
          - temp: 81
            rpm: 81
          - temp: 95
            rpm: 95
  - interval: "10s"
    downSkip: 4
    n: 5
    device:
      ref: "Case Fan"
      sysName: "nct6799"
      slot: "1"
    dependsOn:
      - ref: "Case Temp"
        sysName: "asusec"
        slot: "3"
    curves:
      - ref: "Case Temp"
        points:
          - temp: 38
            rpm: 25
          - temp: 41
            rpm: 35
          - temp: 42
            rpm: 35
          - temp: 46
            rpm: 66
          - temp: 49
            rpm: 75
          - temp: 52
            rpm: 80
          - temp: 60
            rpm: 95
```