package de.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Range;
import de.gigabitzauber.jancontrol.config.JcJacksonConfig;
import de.gigabitzauber.jancontrol.drivers.hwmon.JcHwmonDrivers;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Builder
public record Fan(
    @JsonDeserialize(using = JcJacksonConfig.DurationDeserializer.class)
    @JsonSerialize(using = JcJacksonConfig.DurationSerializer.class)
    Duration interval,
    @JsonDeserialize(using = JcJacksonConfig.JcHwmonDriverDeserializer.class)
    JcHwmonDriver hwmonDriver,
    boolean allowIdle,
    RpmDevice device,
    Collection<Curve> curves,
    List<TemperatureDevice> dependsOn) {

    public static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(5);
    public static final Range<Integer> DEFAULT_SAFETY_MARGIN = Range.closed(20, 100);
    public static final Range<Integer> MIN_SAFETY_MARGIN = Range.closed(0, 100);

    public Fan {
        if (interval == null) {
            interval = DEFAULT_INTERVAL;
        }

        if (hwmonDriver == null) {
            hwmonDriver = JcHwmonDrivers.NCT6775;
        }

        if (curves == null) {
            curves = Set.of();
        }
        curves = Set.copyOf(curves);

        if (dependsOn == null) {
            dependsOn = List.of();
        }
        dependsOn = List.copyOf(dependsOn);
    }

    @JsonIgnore
    public FanMode getCurrentMode() {
        var modeFileHandle = constructModeFileHandle();
        var rawModeValue = modeFileHandle.readRaw().strip();

        return Optional.ofNullable(hwmonDriver.toFanMode(rawModeValue))
            .orElseThrow(() ->
                new IllegalArgumentException("%s contains fan mode unknown to configured driver '%s': %s"
                    .formatted(modeFileHandle.getSysPath(), hwmonDriver.name(), rawModeValue)));
    }

    @JsonIgnore
    public void setMode(FanMode newMode) {
        constructModeFileHandle().writeRaw(newMode.rawValue());
    }

    @JsonIgnore
    public void activateManualMode() {
        setMode(hwmonDriver().manualMode());
    }

    @JsonIgnore
    public Range<Integer> rpmSafetyMargin() {
        if (allowIdle) {
            return MIN_SAFETY_MARGIN;
        } else {
            return DEFAULT_SAFETY_MARGIN;
        }
    }

    private @NonNull RwSysFile constructModeFileHandle() {
        return new RwSysFile(device().getSysPath() + "_enable");
    }
}
