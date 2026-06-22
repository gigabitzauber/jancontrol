package de.mosig.gigabitzauber.jancontrol;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import de.mosig.gigabitzauber.jancontrol.command.RunFanCommand;

@SpringBootApplication
public class JanControlApplication implements CommandLineRunner {

    private final RunFanCommand runFanCommand;

    @Autowired
    public JanControlApplication(RunFanCommand runFanCommand) {
        this.runFanCommand = runFanCommand;
    }

    public static void main(String[] args) {
        SpringApplication.run(JanControlApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java -jar jancontrol.jar run-fan <speed>");
            System.exit(0);
        }

        switch (args[0]) {
            case "run-fan":
                int speed = args.length > 1 ? Integer.parseInt(args[1]) : 1;
                runFanCommand.execute(speed);
                break;
            case "help":
            default:
                System.out.println("Available commands: run-fan <speed>");
        }

        System.exit(0);
    }
}
