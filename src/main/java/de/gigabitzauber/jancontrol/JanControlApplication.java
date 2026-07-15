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

import java.util.Arrays;

@SpringBootApplication
public class JanControlApplication implements CommandLineRunner {

    private static final String MY_VER = "0.2.4";
    private static final String MY_PACKAGE = "de.gigabitzauber.jancontrol";
    private static final String VERBOSE_FLAG = "-v";

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
        if (flagActive(args, "--version")) {
            printVersion();
            System.exit(0);
        }

        if (args.length == 0 || flagActive(args, "-h") || flagActive(args, "--help") || noConfigFileSpecified(args)) {
            printVersion();
            System.err.println();
            System.err.println("Usage: java -jar jancontrol.jar [options] <config-file>");
            System.err.println();
            System.err.println("Options:");
            System.err.println("-h | --help ... show this help");
            System.err.println("-v ... activate verbose mode");
            System.err.println("--version ... show version");
            System.exit(0);
        }

        boolean verboseFlag = flagActive(args, "-v");
        new SpringApplicationBuilder(JanControlApplication.class)
            .logStartupInfo(verboseFlag)
            .run(args);
    }

    private static void printVersion() {
        System.err.println("jancontrol v" + MY_VER);
    }

    private static boolean noConfigFileSpecified(String[] args) {
        return args.length == 0 || args[args.length - 1].startsWith("-");
    }

    private static boolean flagActive(String[] args, String flag) {
        return Arrays.asList(args).contains(flag);
    }

    @Override
    public void run(String @NonNull ... args) {
        if (flagActive(args, VERBOSE_FLAG)) {
            loggingSystem.setLogLevel(MY_PACKAGE, LogLevel.DEBUG);
        }

        var rawConfigFilePath = args[args.length - 1];
        var configResource = new FileSystemResource(rawConfigFilePath);
        var config = configReader.readConfig(configResource);
        cruiseCommand.execute(config);
    }
}
