package de.gigabitzauber.jancontrol.cucumber;

import lombok.Data;

import java.nio.file.Path;

@Data
public class JcCucumberConfigFileData {
    private Path testDataDirPath;
    private Path configFilePath;
}
