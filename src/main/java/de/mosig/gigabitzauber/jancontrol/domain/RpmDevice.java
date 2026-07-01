package de.mosig.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
        var rawValue = Integer.toString(value);
        JcIoUtil.writeString(safeSysPath(), rawValue);
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
