package com.aayushatharva.sourcecenginequerycacher.utils;

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
    public static Long GameUpdateInterval = 1000L;

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
    public static Integer ReceiveAllocatorBufferSizeMax = 256 * 1024 - 1;

    // Stats
    public static boolean Stats_PPS = false;
    public static boolean Stats_BPS = false;

    // Protocols
    public static boolean EnableA2SRule = false;
    public static boolean EnableA2SInfoChallenge = true;

    private static final String HELP = "help";
    private static final String CONFIG = "config";
    private static final String THREADS = "threads";
    private static final String PPS = "pps";
    private static final String BPS = "bps";
    private static final String UPDATE_RATE = "updateRate";
    private static final String CHALLENGE_TTL = "challengeTTL";
    private static final String GAME_IP = "gameip";
    private static final String GAME_PORT = "gameport";
    private static final String ADDR = "addr";
    private static final String PORT = "port";
    private static final String SEND_BUF = "sendBuf";
    private static final String RECV_BUF = "recvBuf";
    private static final String RECV_ALLOC_BUF = "recvAllocatorBuf";
    private static final String RECV_ALLOC_BUF_MAX = "recvAllocatorBufMax";
    private static final String A2S_INFO_CHALLENGE = "a2sInfoChallenge";
    private static final String A2S_RULE = "a2sRule";

    static {
        options = new Options()
                /* General Configuration */
                .addOption(HELP, false, "Display Usages")
                .addOption("-c", CONFIG, true, "Configuration File Path")
                .addOption(THREADS, true, "Number of Threads")
                .addOption(PPS, false, "Enable Packets per Second Stats")
                .addOption(BPS, false, "Enable Bits per Second Stats")
                .addOption(UPDATE_RATE, true, "Game Server Info Update retrieval interval in Milliseconds")

                /* Challenge Code */
                .addOption(CHALLENGE_TTL, true, "Maximum Validity of Challenge Code in Milliseconds (best practise is 2000)")

                /* IP Addresses and Ports */
                .addOption(GAME_IP, true, "Game Server IP Address")
                .addOption(GAME_PORT, true, "Game Server Port")
                .addOption(ADDR, true, "Local Server IP Address on which Cacher Server will bind and listen")
                .addOption(PORT, true, "Local Server Port on which Cacher Server will bind and listen")

                /* Buffers */
                .addOption(SEND_BUF, true, "Server Send Buffer Size")
                .addOption(RECV_BUF, true, "Server Receive Buffer Size")
                .addOption(RECV_ALLOC_BUF, true, "Initial Receive ByteBuf Allocator Buffer Size (must be smaller than Max)")
                .addOption(RECV_ALLOC_BUF_MAX, true, "Maximum Receive ByteBuf Allocator Buffer Size")

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
            logger.atInfo().log("Using Configuration from Path: " + cmd.getOptionValue(CONFIG));

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
        Stats_BPS = Boolean.parseBoolean(Data.getProperty(BPS, String.valueOf(Stats_PPS)));

        GameUpdateInterval = Long.parseLong(Data.getProperty(UPDATE_RATE, String.valueOf(GameUpdateInterval)));
        ChallengeCodeTTL = Long.parseLong(Data.getProperty(CHALLENGE_TTL, String.valueOf(ChallengeCodeTTL)));

        LocalServer = new InetSocketAddress(InetAddress.getByName(Data.getProperty("LocalServerIPAddress",
                InetAddress.getLoopbackAddress().getHostAddress())), Integer.parseInt(Data.getProperty("LocalServerPort", "27016")));
        GameServer = new InetSocketAddress(InetAddress.getByName(Data.getProperty("GameServerIPAddress",
                InetAddress.getLoopbackAddress().getHostAddress())), Integer.parseInt(Data.getProperty("GameServerPort", "27015")));

        SendBufferSize = Integer.parseInt(Data.getProperty("SendBufferSize", String.valueOf(SendBufferSize)));
        ReceiveAllocatorBufferSize = Math.min(Integer.parseInt(Data.getProperty("ReceiveAllocatorBufferSize", String.valueOf(ReceiveAllocatorBufferSize))),
                ReceiveAllocatorBufferSizeMax);
        ReceiveAllocatorBufferSizeMax = Integer.parseInt(Data.getProperty("ReceiveAllocatorBufferSizeMax", String.valueOf(ReceiveAllocatorBufferSizeMax)));
        ReceiveBufferSize = Integer.parseInt(Data.getProperty("ReceiveBufferSize", String.valueOf(ReceiveBufferSize)));

        EnableA2SRule = Boolean.parseBoolean(Data.getProperty("A2SRule", String.valueOf(EnableA2SRule)));
        EnableA2SInfoChallenge = Boolean.parseBoolean(Data.getProperty("A2SInfoChallenge", String.valueOf(EnableA2SInfoChallenge)));
    }

    private static void displayConfig() {
        logger.info("----------------- CONFIGURATION -----------------");
        logger.info("Threads: " + Threads);
        logger.info("PPS: " + Stats_PPS);
        logger.info("bPS: " + Stats_PPS);

        logger.info("GameUpdateRate: " + GameUpdateInterval);
        logger.info("ChallengeCodeTTL: " + ChallengeCodeTTL);

        logger.info("LocalServerIPAddress: " + LocalServer.getAddress().getHostAddress());
        logger.info("LocalServerPort: " + LocalServer.getPort());
        logger.info("GameServerIPAddress: " + GameServer.getAddress().getHostAddress());
        logger.info("GameServerPort: " + GameServer.getPort());

        logger.info("ReceiveBufferSize: " + ReceiveBufferSize);
        logger.info("SendBufferSize: " + SendBufferSize);
        logger.info("ReceiveAllocatorBufferSize: " + ReceiveAllocatorBufferSize);
        logger.info("ReceiveAllocatorBufferSizeMax: " + ReceiveAllocatorBufferSizeMax);
        logger.info("-------------------------------------------------");
    }

    private static void displayHelpAndExit() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("java -jar FILENAME <USAGES ARGUMENTS>", options);

        System.exit(0);
    }
}
