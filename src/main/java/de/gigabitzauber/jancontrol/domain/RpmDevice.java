package de.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Range;
import de.gigabitzauber.jancontrol.config.JcJacksonConfig;
import de.gigabitzauber.jancontrol.domain.api.FanMode;
import de.gigabitzauber.jancontrol.domain.api.HwmonDevice;
import de.gigabitzauber.jancontrol.domain.api.JcHwmonDriver;
import de.gigabitzauber.jancontrol.domain.api.TypedReadableDevice;
import de.gigabitzauber.jancontrol.domain.api.TypedWriteableDevice;
import de.gigabitzauber.jancontrol.drivers.hwmon.JcHwmonDrivers;
import de.gigabitzauber.jancontrol.error.JcException;
import de.gigabitzauber.jancontrol.util.JcIoUtil;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Data
@SuperBuilder
@ToString(callSuper = true)
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
public final class RpmDevice extends HwmonDevice implements TypedReadableDevice<Integer>, TypedWriteableDevice<Integer> {
    static final int DEFAULT_ACTIVATION_THRESHOLD_PERCENT = 20;
    /*
     * Seems like RPM values are stored in a byte. Full speed = 255, half speed = 127 etc.
     */
    private static final int HIGHEST_POSSIBLE_RAW_RPM_VALUE = 255;

    private static final int WRITE_LOWER_BOUND = 0;
    private static final int WRITE_UPPER_BOUND = 100;

    private static final Range<Integer> VALID_WRITE_RANGE = Range.closed(WRITE_LOWER_BOUND, WRITE_UPPER_BOUND);
    private static final Range<Integer> VALID_READ_RANGE = Range.closed(0, 255);

    @Builder.Default
    @JsonDeserialize(using = JcJacksonConfig.JcHwmonDriverDeserializer.class)
    private final JcHwmonDriver driver = JcHwmonDrivers.NCT6775;
    @Builder.Default
    private final boolean allowIdle = false;
    @Builder.Default
    // We want to define min on purpose to not cause any confusion whether it is 0 or not.
    @SuppressWarnings("DefaultAnnotationParam")
    @Size(min = WRITE_LOWER_BOUND, max = WRITE_UPPER_BOUND)
    private final int activationThreshold = DEFAULT_ACTIVATION_THRESHOLD_PERCENT;

    @Override
    @JsonIgnore
    public Integer write(Integer targetValue) {
        requireNonNull(targetValue, "targetValue must not be null");

        if (VALID_WRITE_RANGE.contains(targetValue)) {
            var rawValue = 0;
            if (targetValue >= this.activationThreshold) {
                rawValue = (int) Math.ceil(((double) targetValue / 100) * HIGHEST_POSSIBLE_RAW_RPM_VALUE);
            }
            JcIoUtil.writeString(safeWritableSysPath(), rawValue + "");
            return (int) Math.floor(((double) rawValue / HIGHEST_POSSIBLE_RAW_RPM_VALUE) * 100);
        } else {
            throw new JcException("rpm targetValue out of range [%d, %d]: %s"
                .formatted(VALID_WRITE_RANGE.lowerEndpoint(), VALID_WRITE_RANGE.upperEndpoint(), targetValue));
        }
    }

    @Override
    public Integer read() {
        String rawValue = JcIoUtil.readString(safeReadableSysPath());
        var cleanValueStr = rawValue.strip();
        var readValue = -1;
        try {
            readValue = Integer.parseInt(cleanValueStr);
        } catch (NumberFormatException e) {
            throw new JcException("Value of device '" + ref() + "' is not a number.", e);
        }

        if (VALID_READ_RANGE.contains(readValue)) {
            return (int) Math.floor(((double) readValue / HIGHEST_POSSIBLE_RAW_RPM_VALUE) * 100);
        } else {
            throw new JcException("rpm raw value out of range [%d, %d]: %s"
                .formatted(VALID_READ_RANGE.lowerEndpoint(), VALID_READ_RANGE.upperEndpoint(), readValue));
        }
    }

    @JsonIgnore
    public Range<Integer> safetyMargin() {
        return Range.closed(this.activationThreshold, 100);
    }

    @JsonIgnore
    public FanMode getMode() {
        var modeFileHandle = constructModeFileHandle();
        var rawModeValue = modeFileHandle.readRaw().strip();

        var driver = driver();
        return Optional.ofNullable(driver.toFanMode(rawModeValue))
            .orElseThrow(() ->
                new IllegalArgumentException("%s contains fan mode unknown to configured driver '%s': %s"
                    .formatted(modeFileHandle.sysPath(), driver.name(), rawModeValue)));
    }

    @JsonIgnore
    public void setMode(FanMode newMode) {
        constructModeFileHandle().writeRaw(newMode.rawValue());
    }

    @JsonIgnore
    public FanMode activateManualMode() {
        var manualMode = driver().manualMode();
        setMode(manualMode);

        return manualMode;
    }

    @JsonIgnore
    public void setEmergencyRpm() {
        write(66);
    }

    private @NonNull RwSysFile constructModeFileHandle() {
        return new RwSysFile(sysPath() + "_enable");
    }
}
