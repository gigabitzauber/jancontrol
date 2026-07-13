package de.gigabitzauber.jancontrol;

import de.gigabitzauber.jancontrol.cruise.CruiseCommand;
import de.gigabitzauber.jancontrol.cruise.CruiseConfigReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.core.io.FileSystemResource;

@SpringBootApplication
public class JanControlApplication implements CommandLineRunner {

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
        SpringApplication.run(JanControlApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar jancontrol.jar <config-file>");
            System.exit(0);
        } else if (args.length == 2 && args[1].equals("-v")) {
            loggingSystem.setLogLevel("de.gigabitzauber.jancontrol", LogLevel.DEBUG);
        }

        var configResource = new FileSystemResource(args[0]);
        var config = configReader.readConfig(configResource);
        cruiseCommand.execute(config);


    }
}
