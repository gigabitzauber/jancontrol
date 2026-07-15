# How to use jancontrol

The tool tries to figure out how fast a fan should spin based on one or more temperature values it reads. This is usally nothing more than "if temperature is X, then fan should spin Y rpms". The mapping of temperature value to fan speed is defined in the configuration file. This file contains a [YAML](https://en.wikipedia.org/wiki/YAML) based description of one or more of such fans.

Basically it works like this:

1. Read temperature value from sensors that a fan depends on (called "dependants").
2. Set the desired fan speed based on the curve that has been configured for this fan.
3. Repeat this process every few seconds (configurable per fan as "interval").

Each fan entry defines the following things:

| Key | Meaning |
| --- | --- |
| `interval` | The update interval of this fan. Time units such as `500ms` or `3s` must be used. This is optional and defaults to `5s`. |
| `device` | The target device to control. |
| `device.name` | A human-readable name for the fan. It may contain spaces and is used as a reference for the curves defined for this fan. |
| `device.sysPath` | The path to the kernel device file that controls the fan speed, for example `/sys/devices/platform/nct6775.656/hwmon/hwmon2/pwm2`. |
| `dependsOn` | A list of temperature sensors that this fan should depend on. |
| `dependsOn.name` | The name of a dependency sensor. This is the label used by the configuration. |
| `dependsOn.sysPath` | The sysfs path of the dependency sensor. |
| `curves` | A list of temperature-to-RPM mappings used to interpolate the desired fan speed. Only linear interpolation is currently supported. |
| `curves.ref` | The name of the dependency sensor that this curve references. It must match one of the names defined in `dependsOn`. |
| `curves.points` | A list of points that define the mapping between temperature and fan speed. |
| `curves.points.temp` | The temperature value. The unit can be °C or °F, as long as it is used consistently. |
| `curves.points.rpm` | The desired fan speed value. This is expressed as a percentage from `0` to `100` and is translated to absolute RPMs at runtime. |

For example, the following snippet defines a fan that is controlled from a CPU temperature sensor using a simple linear curve:

```yaml
fans:
  - interval: "3s"
    device:
      name: "CPU Fan"
      sysPath: "/sys/devices/platform/nct6775.656/hwmon/hwmon2/pwm2"
    dependsOn:
      - name: "CPU Temp"
        sysPath: "/sys/devices/platform/nct6775.656/hwmon/hwmon2/temp8_input"
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

### How do multiple temperature sensors for one fan work?

A fan has exactly one `device` and one or more `dependsOn` entries, which are called `dependants`. Each `dependant` may be referenced by at most one curve defined for it. If multiple dependants are defined for a fan, the tool will calculate the desired fan speed for each dependant and then use the **highest value as the final desired fan speed**. This enables scenarios where the fan speed depends on what the machine is currently doing.

For example, this is a real world config file that controls the case fan of a PC based on whether the CPU is currently compiling (GPU / Case cold), or it currently is summer (case hot, CPU & GPU cold) or the machine is currently used for gaming (CPU & GPU hot, case temperature rising):

```yaml
fans:
  - device:
      name: "CPU Fan"
      sysPath: "/sys/devices/platform/nct6775.656/hwmon/hwmon2/pwm2"
    dependsOn:
      - name: "CPU Temp"
        sysPath: "/sys/devices/platform/asus-ec-sensors/hwmon/hwmon7/temp2_input"
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
    interval: "20s"
  - device:
      name: "Case Fan"
      sysPath: "/sys/devices/platform/nct6775.656/hwmon/hwmon2/pwm1"
    dependsOn:
      - name: "Case Temp"
        sysPath: "/sys/devices/platform/nct6775.656/hwmon/hwmon2/temp1_input"
      - name: "GPU Temp"
        sysPath: "/sys/devices/pci0000:00/0000:00:01.1/0000:01:00.0/0000:02:00.0/0000:03:00.0/hwmon/hwmon3/temp1_input"
      - name: "CPU Temp"
        sysPath: "/sys/devices/platform/asus-ec-sensors/hwmon/hwmon7/temp2_input"
    curves:
      - ref: "Case Temp"
        points:
          - temp: 38
            rpm: 25
          - temp: 46
            rpm: 66
          - temp: 49
            rpm: 75
          - temp: 52
            rpm: 80
          - temp: 60
            rpm: 95
      - ref: "GPU Temp"
        type: "linear"
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
    interval: "10s"
```