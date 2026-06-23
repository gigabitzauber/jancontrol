package de.mosig.gigabitzauber.jancontrol.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fan {

    private WriteableDevice device;
    @Builder.Default
    private List<ReadOnlyDevice> dependsOn = new ArrayList<>();
    @Builder.Default
    private Set<CurvePoint> curve = new HashSet<>();
}
