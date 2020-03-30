package com.aayushatharva.sourcecenginequerycacher.gameserver;

import com.aayushatharva.sourcecenginequerycacher.Config;
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
                datagramSocket.setSoTimeout(1000);

                // We'll keep using this Datagram Socket until we hit any exception (Generally SocketTimeoutException).
                while (true) {
                    // Send 'A2S INFO' Packet
                    datagramSocket.send(datagramPacket);

                    // Receive 'A2S INFO' Packet
                    datagramSocket.receive(responsePacket);

                    // Cache the Packet
                    CacheHub.A2S_INFO.set(ByteBuffer.wrap(Arrays.copyOfRange(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength())));

                    // Wait sometime before updating
                    Thread.sleep(Config.GameUpdateInterval);
                }
            } catch (SocketTimeoutException ex) {
                logger.atError().log("Request timed out while fetching latest A2S_Info Update from Game Server");
            } catch (IOException | InterruptedException ex) {
                logger.atError().withThrowable(ex).log("Error occurred");
            }
        }
    }
}
