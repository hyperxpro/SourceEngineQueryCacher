package com.aayushatharva.sourcecenginequerycacher.gameserver;

import com.aayushatharva.sourcecenginequerycacher.Config;
import com.aayushatharva.sourcecenginequerycacher.utils.ByteArrayUtils;
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
public class A2SPLAYER_Worker extends Thread {

    private static final Logger logger = LogManager.getLogger(A2SINFO_Worker.class);

    @Override
    public void run() {
        // 'A2S CHALLENGE' Packet
        final DatagramPacket challengeRequest_Packet = new DatagramPacket(Packets.A2S_PLAYER_CHALLENGE_REQUEST_B, 0, Packets.A2S_PLAYER_CHALLENGE_REQUEST_B.length, Config.GameServerIPAddress, Config.GameServerPort);

        // Allocate 4096 Bytes of Buffer
        final byte[] responseBytes = new byte[4096];
        final DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);

        // We'll keep recreating DatagramSockets whenever it gets destroyed because of any error.
        while (true) {
            try (DatagramSocket datagramSocket = new DatagramSocket()) {

                datagramSocket.setSoTimeout(1000); // Set Socket Timeout of 1 Second

                // We'll keep using this Datagram Socket until we hit any exception (Generally SocketTimeoutException).
                while (true) {
                    // Send 'A2S CHALLENGE' Packet
                    datagramSocket.send(challengeRequest_Packet);

                    // Receive 'A2S CHALLENGE' Packet
                    datagramSocket.receive(responsePacket);

                    // Get Challenge Code out of received 'A2S CHALLENGE' Packet
                    byte[] ChallengeCode = Arrays.copyOfRange(Arrays.copyOfRange(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength()), 5, 9);

                    // Build Challenge Response using Challenge Code received
                    byte[] Response = ByteArrayUtils.joinArrays(Packets.A2S_PLAYER_HEADER, ChallengeCode);

                    // Send Challenge Response Packet
                    datagramSocket.send(new DatagramPacket(Response, Response.length, Config.GameServerIPAddress, Config.GameServerPort));

                    // Receive 'A2S PLAYER' Packet
                    datagramSocket.receive(responsePacket);

                    // Cache the Packet
                    CacheHub.A2S_PLAYER.set(ByteBuffer.wrap(Arrays.copyOfRange(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength())));

                    // Wait 1 Second before querying game server again.
                    Thread.sleep(Config.GameUpdateInterval);
                }
            } catch (SocketTimeoutException ex) {
                logger.atError().log("Request timed out while fetching latest A2S_Player Update from Game Server");
            } catch (IOException | InterruptedException ex) {
                logger.atError().withThrowable(ex).log("Error occurred");
            }
        }
    }
}
