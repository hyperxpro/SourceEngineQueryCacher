package com.aayushatharva.sourcecenginequerycacher.backenddispatchers;

import com.aayushatharva.sourcecenginequerycacher.Main;
import com.aayushatharva.sourcecenginequerycacher.utils.ByteArray;
import com.aayushatharva.sourcecenginequerycacher.utils.CacheHub;
import com.aayushatharva.sourcecenginequerycacher.utils.Packets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.Date;

public class A2S_Player extends Thread {

    @Override
    public void run() {
        // 'A2S CHALLENGE' Packet
        final DatagramPacket challengeRequest_Packet = new DatagramPacket(Packets.A2S_PLAYER_CHALLENGE_REQUEST_B, 0, Packets.A2S_PLAYER_CHALLENGE_REQUEST_B.length, Main.GameServerIP, Main.GameServerPort);

        // Allocate 4096 Bytes of Buffer
        final byte[] responseBytes = new byte[4096];
        final DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);

        // We'll keep recreating DatagramSockets whenever it gets destroyed because of any error.
        while (true) {
            try (DatagramSocket datagramSocket = new DatagramSocket()) {

                datagramSocket.setSoTimeout(1000); // Set Socket Timeout of 1 Second

                // We'll keep using this Datagram Socket until we hit any exception (Generally SocketTimeoutException).
                while (true) {
                    datagramSocket.send(challengeRequest_Packet); // Send 'A2S CHALLENGE' Packet
                    datagramSocket.receive(responsePacket); // Receive 'A2S CHALLENGE' Packet

                    {
                        byte[] ChallengeCode = Arrays.copyOfRange(Arrays.copyOfRange(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength()), 5, 9); // Get Challenge Code out of received 'A2S CHALLENGE' Packet
                        byte[] Response = ByteArray.joinArrays(Packets.A2S_PLAYER_HEADER, ChallengeCode); // Build Challenge Response using Challenge Code received
                        datagramSocket.send(new DatagramPacket(Response, Response.length, Main.GameServerIP, Main.GameServerPort)); // Send Challenge Response Packet
                        datagramSocket.receive(responsePacket); // Receive 'A2S PLAYER' Packet
                    }

                    CacheHub.A2S_PLAYER = Arrays.copyOfRange(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength()); // Save 'A2S PLAYER' Packet into Byte Array

                    Thread.sleep(1000L); // Wait 1 Second before querying game server again.
                }
            } catch (IOException | InterruptedException ex) {
                System.err.println("[" + new Date() + "] [A2S_PLAYER] Error: " + ex.getMessage());
            }
        }
    }
}
