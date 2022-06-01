package com.aayushatharva.seqc.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Properties;

public final class Configuration {

    private static final Logger logger = LogManager.getLogger(Configuration.class);

    /**
     * Server Transport
     */
    public static String TRANSPORT = "Auto";

    /**
     * Server Threads
     */
    public static Integer THREADS = Runtime.getRuntime().availableProcessors();

    /**
     * Game Update Interval
     */
    public static Long UPDATE_RATE;

    /**
     * Challenge TTL
     */
    public static Long CHALLENGE_TTL;

    // IP Addresses and Ports
    public static InetSocketAddress SERVER_ADDRESS;
    public static InetSocketAddress GAME_ADDRESS;

    // Buffers
    public static Integer SEND_BUFFER_SIZE = 1048576;
    public static Integer RECEIVE_BUFFER_SIZE = 1048576;
    public static Integer RECEIVE_ALLOCATOR_SIZE = 128;

    // Stats
    public static boolean STATS_PPS = true;
    public static boolean STATS_BPS = true;

    // Protocols
    public static boolean ENABLE_A2S_INFO_CHALLENGE = false;
    public static boolean ENABLE_A2S_PLAYER = false;
    public static boolean ENABLE_A2S_RULE = false;

    public static void setup(String filename) throws IOException {
        // Parse Config File
        parseConfigFile(filename);

        // Display Configuration
        displayConfig();
    }

    private static void parseConfigFile(String filename) throws IOException {
        try (FileInputStream fis = new FileInputStream(filename)) {
            Properties Data = new Properties();
            Data.load(fis);

            TRANSPORT = Data.getProperty("Transport");
            THREADS = Integer.parseInt(Data.getProperty("Threads"));
            STATS_PPS = Boolean.parseBoolean(Data.getProperty("PPS"));
            STATS_BPS = Boolean.parseBoolean(Data.getProperty("BPS"));

            UPDATE_RATE = Long.parseLong(Data.getProperty("UpdateRate"));
            CHALLENGE_TTL = Long.parseLong(Data.getProperty("ChallengeTTL"));

            SERVER_ADDRESS = new InetSocketAddress(InetAddress.getByName(Data.getProperty("ServerIP")), Integer.parseInt(Data.getProperty("ServerPort")));
            GAME_ADDRESS = new InetSocketAddress(InetAddress.getByName(Data.getProperty("GameIP")), Integer.parseInt(Data.getProperty("GamePort")));

            SEND_BUFFER_SIZE = Integer.parseInt(Data.getProperty("SendBufferSize"));
            RECEIVE_BUFFER_SIZE = Integer.parseInt(Data.getProperty("ReceiveBufferSize"));
            RECEIVE_ALLOCATOR_SIZE = Integer.parseInt(Data.getProperty("ReceiveAllocatorBufferSize"));

            ENABLE_A2S_INFO_CHALLENGE = Boolean.parseBoolean(Data.getProperty("EnableA2SInfoChallenge"));
            ENABLE_A2S_PLAYER = Boolean.parseBoolean(Data.getProperty("EnableA2SPlayer"));
            ENABLE_A2S_RULE = Boolean.parseBoolean(Data.getProperty("EnableA2SRule"));
        } catch (Exception ex) {
            System.out.println("Caught Error: " + ex.getMessage());
            System.exit(1);
        }
    }

    private static void displayConfig() {
        logger.info("----------------- CONFIGURATION -----------------");
        logger.info("Transport: " + TRANSPORT);
        logger.info("Threads: " + THREADS);
        logger.info("PPS: " + STATS_PPS);
        logger.info("BPS: " + STATS_BPS);

        logger.info("UpdateRate: " + UPDATE_RATE);
        logger.info("ChallengeTTL: " + CHALLENGE_TTL);

        logger.info("ServerIP: " + SERVER_ADDRESS.getAddress().getHostAddress());
        logger.info("ServerPort: " + SERVER_ADDRESS.getPort());
        logger.info("GameIP: " + GAME_ADDRESS.getAddress().getHostAddress());
        logger.info("GamePort: " + GAME_ADDRESS.getPort());

        logger.info("SendBufferSize: " + SEND_BUFFER_SIZE);
        logger.info("ReceiveBufferSize: " + RECEIVE_BUFFER_SIZE);
        logger.info("ReceiveAllocatorBufferSize: " + RECEIVE_ALLOCATOR_SIZE);

        logger.info("EnableA2SInfoChallenge: " + ENABLE_A2S_INFO_CHALLENGE);
        logger.info("EnableA2SPlayer: " + ENABLE_A2S_PLAYER);
        logger.info("EnableA2SRule: " + ENABLE_A2S_RULE);
        logger.info("-------------------------------------------------");
    }
}
