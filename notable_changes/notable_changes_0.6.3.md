### Notable Changes

* In case a temperature device cannot be read anymore, setting fan RPM will fail. This may leave fans with too few RPM
  which may cause hardware to overheat. To mitigate this, the fan will be set to 66% RPM and an appropriate log message
  is logged.