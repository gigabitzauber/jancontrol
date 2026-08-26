package de.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.gigabitzauber.jancontrol.domain.api.HwmonDevice;
import de.gigabitzauber.jancontrol.domain.api.TypedReadableDevice;
import de.gigabitzauber.jancontrol.error.JcException;
import de.gigabitzauber.jancontrol.util.JcIoUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString(callSuper = true)
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
public final class TemperatureDevice extends HwmonDevice implements TypedReadableDevice<Integer> {
    private static final int TEMP_CONVERSION_FACTOR = 1000;

    @Override
    @JsonIgnore
    public Integer read() {
        String rawValue = JcIoUtil.readString(safeReadableSysPath());

        var cleanValueStr = rawValue.strip();
        var readValue = -1;
        try {
            readValue = Integer.parseInt(cleanValueStr);
        } catch (NumberFormatException e) {
            throw new JcException("Value of device '" + ref() + "' is not a number.", e);
        }

        return readValue / TEMP_CONVERSION_FACTOR;
    }
}
