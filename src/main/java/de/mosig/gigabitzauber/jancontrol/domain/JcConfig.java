package de.mosig.gigabitzauber.jancontrol.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JcConfig  {
    private Set<Fan> fans = new HashSet<>();
}
