package de.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Range;
import de.gigabitzauber.jancontrol.error.JcException;
import de.gigabitzauber.jancontrol.util.JcIoUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class RpmDevice extends NamedDevice implements TypedReadableDevice<Integer>, TypedWriteableDevice<Integer> {
    static final int DEFAULT_ACTIVATION_THRESHOLD_PERCENT = 20;
    /*
     * Seems like RPM values are stored in a byte. Full speed = 255, half speed = 127 etc.
     */
    private static final int HIGHEST_POSSIBLE_RAW_RPM_VALUE = 255;

    private static final Range<Integer> VALID_WRITE_RANGE = Range.closed(0, 100);
    private static final Range<Integer> VALID_READ_RANGE = Range.closed(0, 255);

    private final int activationThreshold;

    public RpmDevice() {
        super();
        this.activationThreshold = DEFAULT_ACTIVATION_THRESHOLD_PERCENT;
    }

    public RpmDevice(String name, String sysPath) {
        super(name, sysPath);
        this.activationThreshold = DEFAULT_ACTIVATION_THRESHOLD_PERCENT;
    }

    public RpmDevice(String name, String sysPath, int activationThreshold) {
        super(name, sysPath);
        checkArgument(VALID_WRITE_RANGE.contains(activationThreshold),
            "activationThreshold must be in interval [%d, %d]"
                .formatted(VALID_WRITE_RANGE.lowerEndpoint(), VALID_WRITE_RANGE.upperEndpoint()));
        this.activationThreshold = activationThreshold;
    }

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
            throw new JcException("Value of device '" + getName() + "' is not a number.", e);
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
}
