/**
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>
 */
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
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 *
 * @author zh32
 */
public class ServerListPing {

    private InetSocketAddress host;
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
        }

        return response;
    }

    private StatusResponse receiveResponse(DataInputStream dIn) throws IOException {
        readVarInt(dIn); // Packet size is always ignored
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
        String jsonString = new String(responseData, StandardCharsets.UTF_8);
        return gson.fromJson(jsonString, StatusResponse.class);
    }

    private void sendPacket(DataOutputStream out, byte[] data) throws IOException {
        writeVarInt(out, data.length);
        out.write(data);
    }

    private byte[] preparePing() {
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
        out.write(string.getBytes(StandardCharsets.UTF_8));
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

    public void setHost(InetSocketAddress host) {
        this.host = host;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public static class StatusResponse {
        private Players players;
        private Version version;
        private String favicon;
        private int time;

        public Players getPlayers() {
            return players;
        }

        public Version getVersion() {
            return version;
        }

        public String getFavicon() {
            return favicon;
        }

        public int getTime() {
            return time;
        }

        @Override
        public String toString() {
            return "StatusResponse{" +
                    "players=" + players +
                    ", version=" + version +
                    ", favicon='" + favicon + '\'' +
                    ", time=" + time +
                    '}';
        }

        public class Players {
            private int max;
            private int online;
            private List<Player> sample;

            public int getMax() {
                return max;
            }

            public int getOnline() {
                return online;
            }

            public List<Player> getSample() {
                return sample;
            }

            @Override
            public String toString() {
                return "Players{" +
                        "max=" + max +
                        ", online=" + online +
                        ", sample=" + sample +
                        '}';
            }
        }

        public static class Player {
            private String name;
            private String id;

            public String getName() {
                return name;
            }

            public String getId() {
                return id;
            }

            @Override
            public String toString() {
                return "Player{" +
                        "name='" + name + '\'' +
                        ", id='" + id + '\'' +
                        '}';
            }
        }

        public static class Version {
            private String name;
            private String protocol;

            public String getName() {
                return name;
            }

            public String getProtocol() {
                return protocol;
            }

            @Override
            public String toString() {
                return "Version{" +
                        "name='" + name + '\'' +
                        ", protocol='" + protocol + '\'' +
                        '}';
            }
        }
    }
}