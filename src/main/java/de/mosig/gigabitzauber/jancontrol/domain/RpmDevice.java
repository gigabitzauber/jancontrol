package de.mosig.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Range;
import de.mosig.gigabitzauber.jancontrol.error.JcException;
import de.mosig.gigabitzauber.jancontrol.util.JcIoUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Objects.requireNonNull;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class RpmDevice extends NamedDevice implements TypedReadableDevice<Integer>, TypedWriteableDevice<Integer> {
    /*
     * Seems like RPM values are stored in a byte. Full speed = 255, half speed = 127 etc.
     */
    private static final int HIGHEST_POSSIBLE_RAW_RPM_VALUE = 255;

    public RpmDevice() {
        super();
    }

    public RpmDevice(String name, String sysPath) {
        super(name, sysPath);
    }

    @Override
    @JsonIgnore
    public void write(Integer value) {
        requireNonNull(value, "value must not be null");

        if (Range.closed(0, 100).contains(value)) {
            var rawValue = (int) Math.ceil(((double) value / 100) * HIGHEST_POSSIBLE_RAW_RPM_VALUE);
            JcIoUtil.writeString(safeSysPath(), rawValue + "");
        } else {
            throw new JcException("rpm value out of range [0, 100]: " + value);
        }

    }

    @Override
    public Integer read() {
        String rawValue = JcIoUtil.readString(safeSysPath());
        var cleanValueStr = rawValue.strip();
        var readValue = -1;
        try {
            readValue = Integer.parseInt(cleanValueStr);
        } catch (NumberFormatException e) {
            throw new JcException("Value of device '" + getName() + "' is not a number.", e);
        }

        return readValue;
    }
}
