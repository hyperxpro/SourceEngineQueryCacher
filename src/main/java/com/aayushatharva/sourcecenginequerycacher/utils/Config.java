package com.aayushatharva.sourcecenginequerycacher.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Properties;

public final class Config {

    private static final Logger logger = LogManager.getLogger(Config.class);
    private static final Options options;

    /**
     * Cacher Server Threads
     */
    public static Integer Threads = 2;

    /**
     * Game Update Interval
     */
    public static Long GameUpdateInterval = 1000L;

    /**
     * Game Update Socket Timeout
     */
    public static Integer GameUpdateSocketTimeout = 1000;

    /**
     * Challenge Code Validity
     */
    public static Long ChallengeCodeTTL = 2000L;

    // IP Addresses and Ports
    public static InetSocketAddress LocalServer = new InetSocketAddress(InetAddress.getLoopbackAddress(), 27016);
    public static InetSocketAddress GameServer = new InetSocketAddress(InetAddress.getLoopbackAddress(), 27015);

    // Buffers
    public static Integer ReceiveBufferSize = 1048576;
    public static Integer SendBufferSize = 1048576;
    public static Integer ReceiveAllocatorBufferSizeMin = 20480; //leave this bigger than the standard MTU of 1500
    public static Integer ReceiveAllocatorBufferSize = 65535;
    public static Integer ReceiveAllocatorBufferSizeMax = 1048576;
    // Stats
    public static boolean Stats_PPS = false;
    public static boolean Stats_bPS = false;

    //LogLevel for handling Log Level changes without interfering with the log4j2.xml inside the .jar
    public static Level LogLevel = Level.INFO;

    static {
        options = new Options()
                /*General Configuration*/
                .addOption("h", "help", false, "Display Usages")
                .addOption("c", "config", true, "Configuration File Path")
                .addOption("w", "threads", true, "Number of Threads")
                .addOption("p", "ppsStats", false, "Enable Packets per Second Stats")
                .addOption("b", "bpsStats", false, "Enable Bits per Second Stats")


                .addOption("gameUpdateRate", true, "Game Server Info Update retrieval interval in Milliseconds")
                .addOption("gameUpdateTimeout", true, "Game Server Info Update Socket Timeout in Milliseconds")

                /* Challenge Code */
                .addOption("challengeCodeTTL", true, "Maximum Validity of Challenge Code in Milliseconds (best practise is 2000)")

                /* IP Addresses and Ports */
                .addOption("gameip", true, "Game Server IP Address")
                .addOption("gameport", true, "Game Server Port")
                .addOption("bind", true, "Local Server IP Address on which Cacher Server will bind and listen")
                .addOption("port", true, "Local Server Port on which Cacher Server will bind and listen")

                /* Buffers */
                .addOption("r", "receiveBuf", true, "Server Receive Buffer Size")
                .addOption("s", "sendBuf", true, "Server Send Buffer Size")
                .addOption("a0", "receiveAllocatorBuf", true, "Initial Receive ByteBuf Allocator Buffer Size (must be smaller than Max)")
                .addOption("a1", "receiveAllocatorBufMax", true, "Maximum Receive ByteBuf Allocator Buffer Size")
                /* LogLevel */
                .addOption("s", "logLevel", true, "Change the Log output verbosity. In order of most to least Verbose: [ALL,TRACE,DEBUG,INFO,WARN,ERROR,FATAL,OFF]");
    }


    public static void setup(String[] args) throws ParseException, IOException {

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("help")) {
            displayHelpAndExit();
        }

        /*
         * If `config` Parameter is present, parse the config file and load configuration.
         */
        if (cmd.getOptionValue("config") != null) {
            logger.atInfo().log("Using Configuration from Path: " + cmd.getOptionValue("config"));

            // Parse Config File
            parseConfigFile(cmd.getOptionValue("config"));
        } else {

            if (cmd.getOptionValue("threads") != null) {
                Threads = Integer.parseInt(cmd.getOptionValue("threads"));
            }

            if (cmd.hasOption("ppsStats")) {
                Stats_PPS = true;
            }

            if (cmd.hasOption("bpsStats")) {
                Stats_bPS = true;
            }

            if (cmd.getOptionValue("gameUpdateRate") != null) {
                GameUpdateInterval = Long.parseLong(cmd.getOptionValue("gameUpdateRate"));
            }

            if (cmd.getOptionValue("gameUpdateTimeout") != null) {
                GameUpdateSocketTimeout = Integer.parseInt(cmd.getOptionValue("gameUpdateTimeout"));
            }

            if (cmd.getOptionValue("challengeCodeTTL") != null) {
                ChallengeCodeTTL = Long.parseLong(cmd.getOptionValue("challengeCodeTTL"));
            }

            InetAddress GameServerIPAddress = InetAddress.getLoopbackAddress();
            if (cmd.getOptionValue("gameip") != null) {
                GameServerIPAddress = InetAddress.getByName(cmd.getOptionValue("gameip"));
            }

            int GameServerPort = 27015;
            if (cmd.getOptionValue("gameport") != null) {
                GameServerPort = Integer.parseInt(cmd.getOptionValue("gameport"));
            }

            GameServer = new InetSocketAddress(GameServerIPAddress, GameServerPort);

            InetAddress LocalServerIPAddress = InetAddress.getLoopbackAddress();
            if (cmd.getOptionValue("bind") != null) {
                LocalServerIPAddress = InetAddress.getByName(cmd.getOptionValue("bind"));
            }

            int Port = 27016;
            if (cmd.getOptionValue("port") != null) {
                Port = Integer.parseInt(cmd.getOptionValue("port"));
            }

            LocalServer = new InetSocketAddress(LocalServerIPAddress, Port);

            if (cmd.getOptionValue("receiveBuf") != null) {
                ReceiveBufferSize = Integer.parseInt(cmd.getOptionValue("receiveBuf"));
            }

            if (cmd.getOptionValue("sendBuf") != null) {
                SendBufferSize = Integer.parseInt(cmd.getOptionValue("sendBuf"));
            }


            if (cmd.getOptionValue("receiveAllocatorBufMax") != null) {
                ReceiveAllocatorBufferSizeMax = Integer.parseInt(cmd.getOptionValue("receiveAllocatorBufMax"));
            }
            if (cmd.getOptionValue("receiveAllocatorBuf") != null) {
                ReceiveAllocatorBufferSize = Math.min(Integer.parseInt(cmd.getOptionValue("receiveAllocatorBuf")),ReceiveAllocatorBufferSizeMax);
            }

            if (cmd.getOptionValue("logLevel") != null) {
                LogLevel = Level.toLevel(cmd.getOptionValue("logLevel"));
            }

        }
        //Set Log Level Programmatically without relying on the log4J2.xml
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig("com.aayushatharva.sourcecenginequerycacher");
        loggerConfig.setLevel(LogLevel);
        ctx.updateLoggers();

        displayConfig();
    }

    private static void parseConfigFile(String path) throws IOException {
        Properties Data = new Properties();
        Data.load(new FileInputStream(path));

        // Load all Data
        Threads = Integer.parseInt(Data.getProperty("Threads", String.valueOf(Threads)));
        Stats_PPS = Boolean.parseBoolean(Data.getProperty("StatsPPS", String.valueOf(Stats_PPS)));
        Stats_bPS = Boolean.parseBoolean(Data.getProperty("StatsbPS", String.valueOf(Stats_PPS)));

        GameUpdateInterval = Long.parseLong(Data.getProperty("GameUpdateRate", String.valueOf(GameUpdateInterval)));
        GameUpdateSocketTimeout = Integer.parseInt(Data.getProperty("GameUpdateTimeout", String.valueOf(GameUpdateSocketTimeout)));

        ChallengeCodeTTL = Long.parseLong(Data.getProperty("ChallengeCodeTTL", String.valueOf(ChallengeCodeTTL)));

        LocalServer = new InetSocketAddress(InetAddress.getByName(Data.getProperty("LocalServerIPAddress",
                InetAddress.getLoopbackAddress().getHostAddress())), Integer.parseInt(Data.getProperty("LocalServerPort",
                "27016")));
        GameServer  = new InetSocketAddress(InetAddress.getByName(Data.getProperty("GameServerIPAddress",
                InetAddress.getLoopbackAddress().getHostAddress())), Integer.parseInt(Data.getProperty("GameServerPort",
                "27015")));

        ReceiveBufferSize = Integer.parseInt(Data.getProperty("ReceiveBufferSize", String.valueOf(ReceiveBufferSize)));
        SendBufferSize = Integer.parseInt(Data.getProperty("SendBufferSize", String.valueOf(SendBufferSize)));
        ReceiveAllocatorBufferSizeMax = Integer.parseInt(Data.getProperty("ReceiveAllocatorBufferSizeMax", String.valueOf(ReceiveAllocatorBufferSizeMax)));
        ReceiveAllocatorBufferSize = Math.min(Integer.parseInt(Data.getProperty("ReceiveAllocatorBufferSize", String.valueOf(ReceiveAllocatorBufferSize))),ReceiveAllocatorBufferSizeMax);

        LogLevel = Level.toLevel(Data.getProperty("LogLevel", LogLevel.toString()));

        Data.clear(); // Clear Properties
    }

    private static void displayConfig() {
        logger.info("----------------- CONFIGURATION -----------------");
        logger.info("Threads: " + Threads);
        logger.info("PPS: " + Stats_PPS);
        logger.info("bPS: " + Stats_bPS);

        logger.info("GameUpdateRate: " + GameUpdateInterval);
        logger.info("GameUpdateTimeout: " + GameUpdateSocketTimeout);

        logger.info("ChallengeCodeTTL: " + ChallengeCodeTTL);

        logger.info("LocalServerIPAddress: " + LocalServer.getAddress().getHostAddress());
        logger.info("LocalServerPort: " + LocalServer.getPort());
        logger.info("GameServerIPAddress: " + GameServer.getAddress().getHostAddress());
        logger.info("GameServerPort: " + GameServer.getPort());

        logger.info("ReceiveBufferSize: " + ReceiveBufferSize);
        logger.info("SendBufferSize: " + SendBufferSize);
        logger.info("ReceiveAllocatorBufferSize: " + ReceiveAllocatorBufferSize);
        logger.info("ReceiveAllocatorBufferSizeMax: " + ReceiveAllocatorBufferSizeMax);

        logger.info("LogLevel: " + logger.getLevel().toString());
        logger.info("-------------------------------------------------");
    }

    private static void displayHelpAndExit() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("java -jar FILENAME <USAGES ARGUMENTS>", options);

        System.exit(0);
    }
}
