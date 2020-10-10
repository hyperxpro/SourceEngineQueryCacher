package com.aayushatharva.sourcecenginequerycacher.utils;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
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
     * Maximum Challenge Code in Cache
     */
    public static Long MaxChallengeCode = 100000L;

    /**
     * Challenge Code Cache Cleaner Interval
     */
    public static Long ChallengeCodeCacheCleanerInterval = 1000L;

    /**
     * Challenge Code Validity
     */
    public static Long ChallengeCodeTTL = 5000L;

    /**
     * Challenge Code Cache Concurrency
     */
    public static int ChallengeCodeCacheConcurrency = 8;

    // IP Addresses and Ports
    public static InetSocketAddress LocalServer = new InetSocketAddress(InetAddress.getLoopbackAddress(), 27016);
    public static InetSocketAddress GameServer = new InetSocketAddress(InetAddress.getLoopbackAddress(), 27015);

    // Buffers
    public static Integer ReceiveBufferSize = 65535;
    public static Integer SendBufferSize = 65535;
    public static Integer FixedReceiveAllocatorBufferSize = 65535;

    // Stats
    public static boolean Stats_PPS = false;
    public static boolean Stats_bPS = false;

    static {
        options = new Options()
                /*General Configuration*/
                .addOption("h", "help", false, "Display Usages")
                .addOption("c", "config", true, "Configuration File Path")
                .addOption("w", "threads", true, "Number of Threads")
                .addOption("p", "ppsStats", false, "Enable Packets per Second Stats")
                .addOption("b", "bpsStats", false, "Enable Bits per Second Stats")


                .addOption("gameUpdateRate", true, "Game Server Update rate in Milliseconds")
                .addOption("gameUpdateTimeout", true, "Game Server Update Socket Timeout in Milliseconds")

                /* Challenge Code */
                .addOption("maxChallengeCode", true, "Maximum Challenge Codes to be saved")
                .addOption("challengeCodeCacheCleaner", true, "Challenge Code Cache Cleaner Interval in Milliseconds")
                .addOption("challengeCodeTTL", true, "Maximum Validity of Challenge Code in Milliseconds")
                .addOption("challengeCodeCacheConcurrency", true, "Challenge Code Cache Concurrency")

                /* IP Addresses and Ports */
                .addOption("gameip", true, "Game Server IP Address")
                .addOption("gameport", true, "Game Server Port")
                .addOption("bind", true, "Local Server IP Address on which Cacher Server will bind and listen")
                .addOption("port", true, "Local Server Port on which Cacher Server will bind and listen")

                /* Buffers */
                .addOption("r", "receiveBuf", true, "Server Receive Buffer Size")
                .addOption("s", "sendBuf", true, "Server Send Buffer Size")
                .addOption("a", "receiveAllocatorBuf", true, "Fixed Receive ByteBuf Allocator Buffer Size");
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

            if (cmd.getOptionValue("update") != null) {
                GameUpdateInterval = Long.parseLong(cmd.getOptionValue("update"));
            }

            if (cmd.getOptionValue("gameUpdateTimeout") != null) {
                GameUpdateSocketTimeout = Integer.parseInt(cmd.getOptionValue("gameUpdateTimeout"));
            }

            if (cmd.getOptionValue("maxChallengeCode") != null) {
                MaxChallengeCode = Long.parseLong(cmd.getOptionValue("maxChallengeCode"));
            }

            if (cmd.getOptionValue("challengeCodeCacheCleaner") != null) {
                ChallengeCodeCacheCleanerInterval = Long.parseLong(cmd.getOptionValue("challengeCacheCleaner"));
            }

            if (cmd.getOptionValue("challengeCodeTTL") != null) {
                ChallengeCodeTTL = Long.parseLong(cmd.getOptionValue("challengeCodeTTL"));
            }

            if (cmd.getOptionValue("challengeCodeCacheConcurrency") != null) {
                ChallengeCodeCacheCleanerInterval = Long.parseLong(cmd.getOptionValue("challengeCodeCacheConcurrency"));
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

            if (cmd.getOptionValue("receiveAllocatorBuf") != null) {
                FixedReceiveAllocatorBufferSize = Integer.parseInt(cmd.getOptionValue("receiveAllocatorBuf"));
            }
        }

        if (logger.isDebugEnabled()) {
            displayConfig();
        }
    }

    private static void parseConfigFile(String path) throws IOException {
        Properties Data = new Properties();
        Data.load(new FileInputStream(path));

        // Load all Data
        Threads = Integer.parseInt(Data.getProperty("Threads", String.valueOf(Threads)));
        Stats_PPS = Boolean.parseBoolean(Data.getProperty("StatsPPS", String.valueOf(Stats_PPS)));
        Stats_bPS = Boolean.parseBoolean(Data.getProperty("StatsbPS", String.valueOf(Stats_PPS)));

        GameUpdateInterval = Long.parseLong(Data.getProperty("GameUpdateInterval", String.valueOf(GameUpdateInterval)));
        GameUpdateSocketTimeout = Integer.parseInt(Data.getProperty("GameUpdateSocketTimeout", String.valueOf(GameUpdateSocketTimeout)));

        MaxChallengeCode = Long.parseLong(Data.getProperty("MaxChallengeCode", String.valueOf(MaxChallengeCode)));
        ChallengeCodeCacheCleanerInterval = Long.parseLong(Data.getProperty("ChallengeCacheCleanerInterval",
                String.valueOf(ChallengeCodeCacheCleanerInterval)));
        ChallengeCodeTTL = Long.parseLong(Data.getProperty("ChallengeCodeTTL", String.valueOf(ChallengeCodeTTL)));
        ChallengeCodeCacheConcurrency = Integer.parseInt(Data.getProperty("ChallengeCacheConcurrency",
                String.valueOf(ChallengeCodeCacheConcurrency)));

        LocalServer = new InetSocketAddress(InetAddress.getByName(Data.getProperty("LocalServerIPAddress",
                InetAddress.getLoopbackAddress().getHostAddress())), Integer.parseInt(Data.getProperty("LocalServerPort",
                "27016")));
        GameServer  = new InetSocketAddress(InetAddress.getByName(Data.getProperty("GameServerIPAddress",
                InetAddress.getLoopbackAddress().getHostAddress())), Integer.parseInt(Data.getProperty("GameServerPort",
                "27015")));

        ReceiveBufferSize = Integer.parseInt(Data.getProperty("ReceiveBufferSize", String.valueOf(ReceiveBufferSize)));
        SendBufferSize = Integer.parseInt(Data.getProperty("SendBufferSize", String.valueOf(SendBufferSize)));
        FixedReceiveAllocatorBufferSize = Integer.parseInt(Data.getProperty("FixedReceiveAllocatorBufferSize",
                String.valueOf(FixedReceiveAllocatorBufferSize)));

        Data.clear(); // Clear Properties
    }

    private static void displayConfig() {
        logger.atDebug().log("----------------- CONFIGURATION -----------------");
        logger.atDebug().log("Threads: " + Threads);
        logger.atDebug().log("PPS: " + Stats_PPS);
        logger.atDebug().log("bPS: " + Stats_bPS);


        logger.atDebug().log("GameUpdateInterval: " + GameUpdateInterval);
        logger.atDebug().log("GameUpdateSocketTimeout: " + GameUpdateSocketTimeout);

        logger.atDebug().log("MaxChallengeCode: " + MaxChallengeCode);
        logger.atDebug().log("ChallengeCodeCacheCleanerInterval: " + ChallengeCodeCacheCleanerInterval);
        logger.atDebug().log("ChallengeCodeCacheConcurrency: " + ChallengeCodeCacheConcurrency);

        logger.atDebug().log("LocalServerIPAddress: " + LocalServer.getAddress().getHostAddress());
        logger.atDebug().log("LocalServerPort: " + LocalServer.getPort());
        logger.atDebug().log("GameServerIPAddress: " + GameServer.getAddress().getHostAddress());
        logger.atDebug().log("GameServerPort: " + GameServer.getPort());

        logger.atDebug().log("ReceiveBufferSize: " + ReceiveBufferSize);
        logger.atDebug().log("SendBufferSize: " + SendBufferSize);
        logger.atDebug().log("FixedReceiveAllocatorBufferSize: " + FixedReceiveAllocatorBufferSize);
        logger.atDebug().log("-------------------------------------------------");
    }

    private static void displayHelpAndExit() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("java -jar FILENAME <USAGES ARGUMENTS>", options);

        System.exit(0);
    }
}
