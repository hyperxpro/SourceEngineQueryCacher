package com.aayushatharva.sourcecenginequerycacher.gameserver;

import com.aayushatharva.sourcecenginequerycacher.utils.Config;
import com.aayushatharva.sourcecenginequerycacher.utils.CacheHub;
import com.aayushatharva.sourcecenginequerycacher.utils.Packets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;

@SuppressWarnings("InfiniteLoopStatement")
public class A2SINFO_Worker extends Thread {

    private static final Logger logger = LogManager.getLogger(A2SINFO_Worker.class);

    public A2SINFO_Worker(String name) {
        super(name);
    }

    @Override
    public void run() {
        // 'A2S INFO' Packet
        final DatagramPacket datagramPacket = new DatagramPacket(Packets.A2S_INFO_REQUEST, 0, Packets.A2S_INFO_REQUEST.length, Config.GameServerIPAddress, Config.GameServerPort);

        // Allocate 4096 Bytes of Buffer
        final byte[] responseBytes = new byte[4096];
        final DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);

        // We'll keep recreating DatagramSockets whenever it gets destroyed because of any error.
        while (true) {
            try (DatagramSocket datagramSocket = new DatagramSocket()) {

                // Set Socket Timeout of 1 Second
                datagramSocket.setSoTimeout(Config.GameUpdateSocketTimeout);

                // We'll keep using this Datagram Socket until we hit any exception (Generally SocketTimeoutException).
                while (true) {
                    // Send 'A2S INFO' Packet
                    logger.atDebug().log("Sending A2S_INFO update request to: " + Config.GameServerIPAddress.getHostAddress() + ":" + Config.GameServerPort);
                    datagramSocket.send(datagramPacket);

                    // Receive 'A2S INFO' Packet
                    datagramSocket.receive(responsePacket);
                    logger.atDebug().log("Received A2S_INFO update response from: " + Config.GameServerIPAddress.getHostAddress() + ":" + Config.GameServerPort);

                    // Cache the Packet
                    CacheHub.A2S_INFO.set(ByteBuffer.wrap(Arrays.copyOfRange(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength())));
                    logger.atDebug().log("New A2S_INFO Update Cached Successfully");

                    // Wait sometime before updating
                    logger.atDebug().log("Waiting for " + Config.GameUpdateInterval + " ms. before requesting for A2S_INFO update again");
                    sleep(Config.GameUpdateInterval);
                }
            } catch (SocketTimeoutException ex) {
                logger.atError().log("Request timed out while fetching latest update from Game Server");
            } catch (IOException | InterruptedException ex) {
                logger.atError().withThrowable(ex).log("Error occurred");
            }
        }
    }
}
