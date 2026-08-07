package de.gigabitzauber.jancontrol;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.gigabitzauber.jancontrol.cruise.CruiseCommand;
import de.gigabitzauber.jancontrol.cruise.WatchConfigCommand;
import de.gigabitzauber.jancontrol.domain.CruiseConfig;
import de.gigabitzauber.jancontrol.domain.CruiseConfigRoot;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.core.io.FileSystemResource;

import java.util.Arrays;
import java.util.Set;

@SpringBootApplication
public class JanControlApplication implements CommandLineRunner {

    private static final String MY_VER = "0.6.2-SNAPSHOT";
    private static final String MY_PACKAGE = "de.gigabitzauber.jancontrol";

    private static final String VERBOSE_FLAG = "-v";
    private static final String WATCH_FLAG = "-w";

    private final YAMLMapper mapper;
    private final CruiseCommand cruiseCommand;
    private final WatchConfigCommand watchConfigCommand;
    private final LoggingSystem loggingSystem;

    @Autowired
    public JanControlApplication(YAMLMapper mapper, CruiseCommand cruiseCommand, WatchConfigCommand watchConfigCommand, LoggingSystem loggingSystem) {
        this.mapper = mapper;
        this.cruiseCommand = cruiseCommand;
        this.watchConfigCommand = watchConfigCommand;
        this.loggingSystem = loggingSystem;
    }

    public static void main(String[] args) {
        if (flagActive(args, "--version")) {
            printVersion();
            System.exit(0);
        }

        if (args.length == 0 || flagActive(args, "-h") || flagActive(args, "--help")) {
            printVersion();
            System.err.println();
            System.err.println("Usage: java -jar jancontrol.jar [options] <config-file>");
            System.err.println();
            System.err.println("Options:");
            System.err.println("-h | --help ... show this help");
            System.err.println(WATCH_FLAG + " ... watch config file for changes");
            System.err.println(VERBOSE_FLAG + " ... activate verbose mode");
            System.err.println("--version ... show version");
            System.exit(0);
        }

        boolean verboseFlag = flagActive(args, VERBOSE_FLAG);
        new SpringApplicationBuilder(JanControlApplication.class)
            .logStartupInfo(verboseFlag)
            .run(args);
    }

    private static void printVersion() {
        System.err.println("jancontrol v" + MY_VER);
    }

    private static boolean configFileSpecified(String[] args) {
        return !(args.length == 0 || args[args.length - 1].startsWith("-"));
    }

    private static boolean flagActive(String[] args, String flag) {
        return Arrays.asList(args).contains(flag);
    }

    @Override
    public void run(String @NonNull ... args) {
        if (flagActive(args, VERBOSE_FLAG)) {
            loggingSystem.setLogLevel(MY_PACKAGE, LogLevel.DEBUG);
        }

        Logger logger = LoggerFactory.getLogger(JanControlApplication.class);
        CruiseConfigRoot configRoot = new CruiseConfigRoot(Set.of());

        if (configFileSpecified(args)) {
            var rawConfigFilePath = args[args.length - 1];
            logger.info("Loading configRoot from {}", rawConfigFilePath);
            var configResource = new FileSystemResource(rawConfigFilePath);
            var config = new CruiseConfig(configResource, mapper);
            configRoot = config.load();
            if (flagActive(args, WATCH_FLAG)) {
                logger.info("Watch flag is active. Watching config file for changes.");
                watchConfigCommand.execute(config);
            } else {
                logger.debug("No watch flag found. NOT watching config file for changes.");
            }
        } else {
            logger.warn("No config file specified.");
        }
        cruiseCommand.execute(configRoot);
    }
}
