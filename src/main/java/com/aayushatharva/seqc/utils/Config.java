package com.aayushatharva.seqc.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
    public static Integer Threads = Runtime.getRuntime().availableProcessors();

    /**
     * Game Update Interval
     */
    public static Long UpdateRate = 1000L;

    /**
     * Challenge Code Validity
     */
    public static Long ChallengeTTL = 2000L;

    // IP Addresses and Ports
    // These fields will be initialized during configuration processing
    public static InetSocketAddress ServerAddress;
    public static InetSocketAddress GameAddress;

    // Buffers
    public static Integer SendBufferSize = 1048576;
    public static Integer ReceiveBufferSize = 1048576;
    public static Integer ReceiveAllocatorBufferSize = 65535;
    public static Integer ReceiveAllocatorBufferSizeMin = 20480; //leave this bigger than the standard MTU of 1500
    public static Integer ReceiveAllocatorBufferSizeMax = (256 * 1024) - 1;

    // Stats
    public static boolean Stats_PPS = true;
    public static boolean Stats_BPS = true;

    // Protocols
    public static boolean EnableA2SInfoChallenge = true;
    public static boolean EnableA2SRule = false;

    private static final String HELP = "help";
    private static final String CONFIG = "config";
    private static final String THREADS = "Threads";
    private static final String PPS = "PPS";
    private static final String BPS = "BPS";
    private static final String UPDATE_RATE = "UpdateRate";
    private static final String CHALLENGE_TTL = "ChallengeTTL";
    private static final String GAME_IP = "GameIP";
    private static final String GAME_PORT = "GamePort";
    private static final String SERVER_IP = "ServerIP";
    private static final String SERVER_PORT = "ServerPort";
    private static final String SEND_BUF = "SendBufferSize";
    private static final String RECEIVE_BUF = "ReceiveBufferSize";
    private static final String RECEIVE_ALLOC_BUF = "ReceiveAllocatorBufferSize";
    private static final String RECEIVE_ALLOC_BUF_MAX = "ReceiveAllocatorBufferSizeMax";
    private static final String A2S_INFO_CHALLENGE = "A2SInfoChallenge";
    private static final String A2S_RULE = "A2SRule";

    static {
        options = new Options()
                /* General Configuration */
                .addOption(HELP, false, "Display Usages")
                .addOption("c", CONFIG, true, "Configuration File Path")
                .addOption(THREADS, true, "Number of Threads")
                .addOption(PPS, false, "Enable Packets per Second Stats")
                .addOption(BPS, false, "Enable Bits per Second Stats")
                .addOption(UPDATE_RATE, true, "Game Info Update retrieval rate in Milliseconds")

                /* Challenge Code */
                .addOption(CHALLENGE_TTL, true, "Maximum Validity of Challenge Code in Milliseconds (best practise is 2000)")

                /* IP Addresses and Ports */
                .addOption(GAME_IP, true, "Game Server IP Address")
                .addOption(GAME_PORT, true, "Game Server Port")
                .addOption(SERVER_IP, true, "Server IP Address on which Cacher will bind and listen")
                .addOption(SERVER_PORT, true, "Server Port on which Cacher will bind and listen")

                /* Buffers */
                .addOption(SEND_BUF, true, "Server Send Buffer Size")
                .addOption(RECEIVE_BUF, true, "Server Receive Buffer Size")
                .addOption(RECEIVE_ALLOC_BUF, true, "Initial Receive ByteBuf Allocator Buffer Size (must be smaller than Max)")
                .addOption(RECEIVE_ALLOC_BUF_MAX, true, "Maximum Receive ByteBuf Allocator Buffer Size")

                /* Protocols */
                .addOption(A2S_INFO_CHALLENGE, false, "Enable A2SInfo Challenge")
                .addOption(A2S_RULE, false, "Enable A2SRule Protocol");
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
        if (cmd.getOptionValue(CONFIG) != null) {
            logger.info("Using Configuration from Path: " + cmd.getOptionValue(CONFIG));

            // Parse Config File
            parseConfigFile(cmd.getOptionValue(CONFIG));
        }

        displayConfig();
    }

    private static void parseConfigFile(String path) throws IOException {
        Properties Data = new Properties();
        Data.load(new FileInputStream(path));

        // Load all Data
        Threads = Integer.parseInt(Data.getProperty(THREADS, String.valueOf(Threads)));
        Stats_PPS = Boolean.parseBoolean(Data.getProperty(PPS, String.valueOf(Stats_PPS)));
        Stats_BPS = Boolean.parseBoolean(Data.getProperty(BPS, String.valueOf(Stats_BPS)));

        UpdateRate = Long.parseLong(Data.getProperty(UPDATE_RATE, String.valueOf(UpdateRate)));
        ChallengeTTL = Long.parseLong(Data.getProperty(CHALLENGE_TTL, String.valueOf(ChallengeTTL)));

        ServerAddress = new InetSocketAddress(InetAddress.getByName(Data.getProperty(SERVER_IP, "127.0.0.1")), Integer.parseInt(Data.getProperty(SERVER_PORT, "9110")));
        GameAddress = new InetSocketAddress(InetAddress.getByName(Data.getProperty(SERVER_IP, "127.0.0.1")), Integer.parseInt(Data.getProperty(SERVER_PORT, "27015")));

        SendBufferSize = Integer.parseInt(Data.getProperty(SEND_BUF, String.valueOf(SendBufferSize)));
        ReceiveBufferSize = Integer.parseInt(Data.getProperty(RECEIVE_BUF, String.valueOf(ReceiveBufferSize)));
        ReceiveAllocatorBufferSize = Math.min(Integer.parseInt(Data.getProperty(RECEIVE_ALLOC_BUF, String.valueOf(ReceiveAllocatorBufferSize))), ReceiveAllocatorBufferSizeMax);
        ReceiveAllocatorBufferSizeMax = Integer.parseInt(Data.getProperty(RECEIVE_ALLOC_BUF_MAX, String.valueOf(ReceiveAllocatorBufferSizeMax)));

        EnableA2SInfoChallenge = Boolean.parseBoolean(Data.getProperty(A2S_INFO_CHALLENGE, String.valueOf(EnableA2SInfoChallenge)));
        EnableA2SRule = Boolean.parseBoolean(Data.getProperty(A2S_RULE, String.valueOf(EnableA2SRule)));
    }

    private static void displayConfig() {
        logger.info("----------------- CONFIGURATION -----------------");
        logger.info("Threads: " + Threads);
        logger.info("PPS: " + Stats_PPS);
        logger.info("BPS: " + Stats_BPS);

        logger.info("UpdateRate: " + UpdateRate);
        logger.info("ChallengeTTL: " + ChallengeTTL);

        logger.info("ServerIP: " + ServerAddress.getAddress().getHostAddress());
        logger.info("ServerPort: " + ServerAddress.getPort());
        logger.info("GameIP: " + GameAddress.getAddress().getHostAddress());
        logger.info("GamePort: " + GameAddress.getPort());

        logger.info("SendBufferSize: " + SendBufferSize);
        logger.info("ReceiveBufferSize: " + ReceiveBufferSize);
        logger.info("ReceiveAllocatorBufferSize: " + ReceiveAllocatorBufferSize);
        logger.info("ReceiveAllocatorBufferSizeMax: " + ReceiveAllocatorBufferSizeMax);

        logger.info("A2SInfoChallenge: " + EnableA2SInfoChallenge);
        logger.info("A2SRule: " + EnableA2SRule);
        logger.info("-------------------------------------------------");
    }

    private static void displayHelpAndExit() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("java -jar FILENAME <USAGES ARGUMENTS>", options);

        System.exit(0);
    }
}
