package com.aayushatharva.sourcecenginequerycacher.gameserver;

import com.aayushatharva.sourcecenginequerycacher.Main;
import com.aayushatharva.sourcecenginequerycacher.utils.CacheHub;
import com.aayushatharva.sourcecenginequerycacher.utils.Config;
import com.aayushatharva.sourcecenginequerycacher.utils.Packets;
import com.aayushatharva.sourcecenginequerycacher.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public final class A2SINFO_Worker extends Thread {

    private static final Logger logger = LogManager.getLogger(A2SINFO_Worker.class);
    private boolean keepRunning = true;

    public A2SINFO_Worker(String name) {
        super(name);
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        // 'A2S INFO' Packet
        final DatagramPacket datagramPacket = new DatagramPacket(Packets.A2S_INFO_REQUEST, 0, Packets.A2S_INFO_REQUEST.length,
                Config.GameServerIPAddress, Config.GameServerPort);

        // Allocate 4096 Bytes of Buffer
        final byte[] responseBytes = new byte[4096];
        final DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);

        while (keepRunning) {
            try (DatagramSocket datagramSocket = new DatagramSocket()) {

                datagramSocket.setSoTimeout(Config.GameUpdateSocketTimeout);

                // We'll keep using this Datagram Socket until we hit any exception (Generally SocketTimeoutException).
                while (keepRunning) {
                    // Send 'A2S INFO' Packet
                    logger.atDebug().log("Sending A2S_INFO update request to {}:{}", Config.GameServerIPAddress.getHostAddress(),
                            Config.GameServerPort);
                    datagramSocket.send(datagramPacket);

                    // Receive 'A2S INFO' Packet
                    datagramSocket.receive(responsePacket);
                    logger.atDebug().log("Received A2S_INFO update response from: {}:{}", Config.GameServerIPAddress.getHostAddress(),
                            Config.GameServerPort);

                    // Cache the Packet
                    byte[] response = Arrays.copyOfRange(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength());

                    // Release the ByteBuf
                    Utils.safeRelease(CacheHub.A2S_INFO.get());

                    CacheHub.A2S_INFO.set(Main.BYTE_BUF_ALLOCATOR.directBuffer(response.length).writeBytes(response));
                    logger.atDebug().log("New A2S_INFO Update Cached Successfully");

                    // Wait sometime before updating
                    logger.atDebug().log("Waiting for {} ms. before requesting for A2S_INFO update again", Config.GameUpdateInterval);
                    sleep(Config.GameUpdateInterval);

                    // If false then we're requested to shutdown.
                    if (!keepRunning) {
                        return;
                    }
                }
            } catch (SocketTimeoutException ex) {
                logger.atError().log("Request timed out while fetching latest update from Game Server {}:{}",
                        Config.GameServerIPAddress, Config.GameServerPort);
            } catch (IOException | InterruptedException ex) {
                logger.atError().withThrowable(ex).log("Error occurred");
            }
        }
    }

    public void shutdown() {
        this.interrupt();
        keepRunning = false;
    }
}
