package de.mosig.gigabitzauber.jancontrol.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurvePoint {
    private int temp;
    private int rpm;
}
