package de.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.gigabitzauber.jancontrol.domain.api.ReferableDevice;
import de.gigabitzauber.jancontrol.domain.api.TypedReadableDevice;
import de.gigabitzauber.jancontrol.error.JcException;
import de.gigabitzauber.jancontrol.util.JcIoUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
public final class TemperatureDevice extends ReferableDevice implements TypedReadableDevice<Integer> {
    private static final int TEMP_CONVERSION_FACTOR = 1000;

    public TemperatureDevice() {
        super();
    }

    public TemperatureDevice(String ref, String sysPath) {
        super(ref, sysPath);
    }

    @Override
    @JsonIgnore
    public Integer read() {
        String rawValue = JcIoUtil.readString(safeReadableSysPath());

        var cleanValueStr = rawValue.strip();
        var readValue = -1;
        try {
            readValue = Integer.parseInt(cleanValueStr);
        } catch (NumberFormatException e) {
            throw new JcException("Value of device '" + getRef() + "' is not a number.", e);
        }

        return readValue / TEMP_CONVERSION_FACTOR;
    }
}
