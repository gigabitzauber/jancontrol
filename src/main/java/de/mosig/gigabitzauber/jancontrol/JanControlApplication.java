package de.mosig.gigabitzauber.jancontrol;

import de.mosig.gigabitzauber.jancontrol.command.CruiseCommand;
import de.mosig.gigabitzauber.jancontrol.config.JcConfigReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.FileSystemResource;

@SpringBootApplication
public class JanControlApplication implements CommandLineRunner {

    private final CruiseCommand cruiseCommand;

    @Autowired
    public JanControlApplication(CruiseCommand cruiseCommand) {
        this.cruiseCommand = cruiseCommand;
    }

    public static void main(String[] args) {
        SpringApplication.run(JanControlApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar jancontrol.jar <config-file>");
            System.exit(0);
        }

        var configResource = new FileSystemResource(args[0]);
        var config = new JcConfigReader().readConfig(configResource);
        cruiseCommand.execute(config);

        System.exit(0);
    }
}
