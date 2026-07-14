package de.gigabitzauber.jancontrol;

import de.gigabitzauber.jancontrol.cruise.CruiseCommand;
import de.gigabitzauber.jancontrol.cruise.CruiseConfigReader;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.core.io.FileSystemResource;

@SpringBootApplication
public class JanControlApplication implements CommandLineRunner {

    private static final String MY_PACKAGE = "de.gigabitzauber.jancontrol";

    private final CruiseConfigReader configReader;
    private final CruiseCommand cruiseCommand;
    private final LoggingSystem loggingSystem;

    @Autowired
    public JanControlApplication(CruiseConfigReader configReader, CruiseCommand cruiseCommand, LoggingSystem loggingSystem) {
        this.configReader = configReader;
        this.cruiseCommand = cruiseCommand;
        this.loggingSystem = loggingSystem;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar jancontrol.jar <config-file>");
            System.exit(0);
        }
        boolean startupInfoFlag = verboseEnabled(args);
        new SpringApplicationBuilder(JanControlApplication.class)
            .logStartupInfo(startupInfoFlag)
            .run(args);
    }

    private static boolean verboseEnabled(String[] args) {
        return args.length == 2 && args[1].equals("-v");
    }

    @Override
    public void run(String @NonNull ... args) {
        if (verboseEnabled(args)) {
            loggingSystem.setLogLevel(MY_PACKAGE, LogLevel.DEBUG);
        }

        var configResource = new FileSystemResource(args[0]);
        var config = configReader.readConfig(configResource);
        cruiseCommand.execute(config);
    }
}
