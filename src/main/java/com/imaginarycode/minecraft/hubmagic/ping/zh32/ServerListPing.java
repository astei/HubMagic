package com.imaginarycode.minecraft.hubmagic.ping.zh32;

import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;

import lombok.Data;
import lombok.Setter;

/**
 *
 * @author zh32
 */
public class ServerListPing {

    @Setter
    private InetSocketAddress host;
    @Setter
    private int timeout = 2000;
    private final static Gson gson = new Gson();

    public StatusResponse fetchData() throws IOException {
        Socket socket = null;
        OutputStream oStr = null;
        InputStream inputStream = null;
        StatusResponse response = null;

        try {
            socket = new Socket();
            socket.setSoTimeout(timeout);
            socket.connect(host, timeout);

            oStr = socket.getOutputStream();
            DataOutputStream dataOut = new DataOutputStream(oStr);

            inputStream = socket.getInputStream();
            DataInputStream dIn = new DataInputStream(inputStream);

            sendPacket(dataOut, prepareHandshake());
            sendPacket(dataOut, preparePing());

            response = receiveResponse(dIn);

            dIn.close();
            dataOut.close();
        } finally {
            if (oStr != null) {
                oStr.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
            return response;
        }
    }

    private StatusResponse receiveResponse(DataInputStream dIn) throws IOException {
        int size = readVarInt(dIn);
        int packetId = readVarInt(dIn);

        if (packetId != 0x00) {
            throw new IOException("Invalid packetId");
        }

        int stringLength = readVarInt(dIn);

        if (stringLength < 1) {
            throw new IOException("Invalid string length.");
        }

        byte[] responseData = new byte[stringLength];
        dIn.readFully(responseData);
        String jsonString = new String(responseData, Charset.forName("utf-8"));
        StatusResponse response = gson.fromJson(jsonString, StatusResponse.class);
        return response;
    }

    private void sendPacket(DataOutputStream out, byte[] data) throws IOException {
        writeVarInt(out, data.length);
        out.write(data);
    }

    private byte[] preparePing() throws IOException {
        return new byte[] {0x00};
    }

    private byte[] prepareHandshake() throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DataOutputStream handshake = new DataOutputStream(bOut);
        bOut.write(0x00); //packet id
        writeVarInt(handshake, 4); //protocol version
        writeString(handshake, host.getHostString());
        handshake.writeShort(host.getPort());
        writeVarInt(handshake, 1); //target state 1
        return bOut.toByteArray();
    }

    public void writeString(DataOutputStream out, String string) throws IOException {
        writeVarInt(out, string.length());
        out.write(string.getBytes(Charset.forName("utf-8")));
    }

    public int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) throw new RuntimeException("VarInt too big");
            if ((k & 0x80) != 128) break;
        }
        return i;
    }

    public void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.write(paramInt);
                return;
            }

            out.write(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }

    @Data
    public static class StatusResponse {
        private String description;
        private Players players;
        private Version version;
        private String favicon;
        private int time;

        @Data
        public class Players {
            private int max;
            private int online;
            private List<Player> sample;
        }
        @Data
        public class Player {
            private String name;
            private String id;

        }
        @Data
        public class Version {
            private String name;
            private String protocol;
        }
    }
}