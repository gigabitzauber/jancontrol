package de.mosig.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mosig.gigabitzauber.jancontrol.error.JcException;
import de.mosig.gigabitzauber.jancontrol.util.JcIoUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class TemperatureDevice extends NamedDevice implements TypedReadableDevice<Integer> {
    private static final int TEMP_CONVERSION_FACTOR = 1000;

    public TemperatureDevice() {
        super();
    }

    public TemperatureDevice(String name, String sysPath) {
        super(name, sysPath);
    }

    @Override
    @JsonIgnore
    public Integer read() {
        String rawValue = JcIoUtil.readString(safeSysPath());

        var cleanValueStr = rawValue.strip();
        var readValue = -1;
        try {
            readValue = Integer.parseInt(cleanValueStr);
        } catch (NumberFormatException e) {
            throw new JcException("Value of device '" + getName() + "' is not a number.", e);
        }

        return readValue / TEMP_CONVERSION_FACTOR;
    }
}
