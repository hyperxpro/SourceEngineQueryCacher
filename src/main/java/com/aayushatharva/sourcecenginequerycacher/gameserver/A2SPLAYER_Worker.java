package com.aayushatharva.sourcecenginequerycacher.gameserver;

import com.aayushatharva.sourcecenginequerycacher.Main;
import com.aayushatharva.sourcecenginequerycacher.utils.ByteArrayUtils;
import com.aayushatharva.sourcecenginequerycacher.utils.CacheHub;
import com.aayushatharva.sourcecenginequerycacher.utils.Config;
import com.aayushatharva.sourcecenginequerycacher.utils.Packets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class A2SPLAYER_Worker extends Thread {

    private static final Logger logger = LogManager.getLogger(A2SINFO_Worker.class);
    private boolean keepRunning = true;

    public A2SPLAYER_Worker(String name) {
        super(name);
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        logger.atInfo().log("Startng A2S_Player Worker");

        // 'A2S CHALLENGE' Packet
        final DatagramPacket challengeRequest_Packet = new DatagramPacket(Packets.A2S_PLAYER_CHALLENGE_REQUEST_B, 0,
                Packets.A2S_PLAYER_CHALLENGE_REQUEST_B.length, Config.GameServerIPAddress, Config.GameServerPort);

        // Allocate 4096 Bytes of Buffer
        final byte[] responseBytes = new byte[4096];
        final DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);

        while (keepRunning) {
            try (DatagramSocket datagramSocket = new DatagramSocket()) {

                datagramSocket.setSoTimeout(Config.GameUpdateSocketTimeout);

                // We'll keep using this Datagram Socket until we hit any exception (Generally SocketTimeoutException).
                while (keepRunning) {
                    // Send 'A2S CHALLENGE' Packet
                    logger.atDebug().log("Sending A2S_CHALLENGE request to {}:{}", Config.GameServerIPAddress.getHostAddress(),
                            Config.GameServerPort);
                    datagramSocket.send(challengeRequest_Packet);

                    // Receive 'A2S CHALLENGE' Packet
                    datagramSocket.receive(responsePacket);
                    logger.atDebug().log("Received A2S_CHALLENGE response from {}:{}", Config.GameServerIPAddress.getHostAddress(),
                            Config.GameServerPort);

                    /*
                     * Get Challenge Code out of received 'A2S CHALLENGE' Packet and
                     * build Challenge response using Challenge Code received.
                     */
                    byte[] ChallengeCode = Arrays.copyOfRange(Arrays.copyOfRange(responsePacket.getData(), responsePacket.getOffset(),
                            responsePacket.getLength()), 5, 9);
                    byte[] Response = ByteArrayUtils.joinArrays(Packets.A2S_PLAYER_HEADER, ChallengeCode);

                    // Send Challenge Response Packet
                    logger.atDebug().log("Sending A2S_PLAYER update request along with Challenge Code to {}:{}",
                            Config.GameServerIPAddress.getHostAddress(), Config.GameServerPort);
                    datagramSocket.send(new DatagramPacket(Response, Response.length, Config.GameServerIPAddress, Config.GameServerPort));

                    // Receive 'A2S PLAYER' Packet
                    datagramSocket.receive(responsePacket);
                    logger.atDebug().log("Received A2S_PLAYER update response from {}:{}", Config.GameServerIPAddress.getHostAddress(),
                            Config.GameServerPort);

                    // Cache the Packet
                    byte[] response = Arrays.copyOfRange(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength());
                    if (CacheHub.A2S_PLAYER.get() != null && CacheHub.A2S_PLAYER.get().refCnt() > 0) {
                        CacheHub.A2S_PLAYER.get().release();
                    }
                    CacheHub.A2S_PLAYER.set(Main.alloc.directBuffer(response.length).writeBytes(response));
                    logger.atDebug().log("New A2S_PLAYER Update Cached Successfully");

                    // Wait 1 Second before querying game server again.
                    logger.atDebug().log("Waiting for {} ms. before requesting for A2S_PLAYER update again", Config.GameUpdateInterval);
                    sleep(Config.GameUpdateInterval);

                    // If false then we're requested to shutdown
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
