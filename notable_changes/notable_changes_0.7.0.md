### Notable Changes

Introducing automatic HWMOn name resolution. **ATTENTION:** Breaking changes. Old config files will not be compatible
with releases >= 0.7.x!

* `name` keys have been renamed to `ref`.
* `sysPath` keys will not work anymore.
  ** They have been replaced by the new `sysName` concept.

The device files, jancontrol operates on, are stored in numbered hwmon directories. The numbers of these directories may
change after reboots, rendering the configuration broken. To fix that, jancontrol does not use direct links to the files
anymore but names of the corresponding sensors. See `docs/how_to_use.md` section `How devices are identified` to for
details.

See `/sys/class/hwmon/hwmonN/name` files to find valid names or use lm-sensors.