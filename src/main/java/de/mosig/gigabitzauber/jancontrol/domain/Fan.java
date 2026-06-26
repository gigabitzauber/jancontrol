package de.mosig.gigabitzauber.jancontrol.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fan {

    private WriteableDevice device;
    private Curve curve;
    @Builder.Default
    private List<ReadOnlyDevice> dependsOn = new ArrayList<>();
}
