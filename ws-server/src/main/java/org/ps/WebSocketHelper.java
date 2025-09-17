package org.ps;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

class WebSocketHelper {

    static boolean handshake(Socket socket) throws IOException, NoSuchAlgorithmException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line;
        String webSocketKey = null;
        while (!(line = reader.readLine()).isEmpty()) {
            if (line.toLowerCase().startsWith("sec-websocket-key: ")) {
                webSocketKey = line.substring(19);
                break;
            }
        }
        if (webSocketKey == null) {
            return false;
        }

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update((webSocketKey.trim() + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes());
        String acceptKey = Base64.getEncoder().encodeToString(md.digest());
        String response = "HTTP/1.1 101 Switching Protocols\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";
        OutputStream out = socket.getOutputStream();
        out.write(response.getBytes());
        out.flush();
        return true;
    }

    static WebSocketFrame readFrame(Socket socket) throws IOException {
        InputStream in = socket.getInputStream();
        int firstByte = in.read();
        if (firstByte == -1) {
            return new WebSocketFrame(false, Opcode.OPCODE_CLOSE, new byte[0]);
        }
        int secondByte = in.read();
        if (secondByte == -1) {
            return new WebSocketFrame(false, Opcode.OPCODE_CLOSE, new byte[0]);
        }

        boolean fin = (firstByte & 0x80) != 0;
        Opcode opcode = Opcode.fromCode(firstByte & 0x0F);
        boolean masked = (secondByte & 0x80) != 0;
        int payloadLen = secondByte & 0x7F;

        if (opcode == Opcode.OPCODE_CLOSE) {
            return new WebSocketFrame(false, Opcode.OPCODE_CLOSE, new byte[0]);
        }
        if (!masked) {
            return new WebSocketFrame(false, Opcode.UNMASKED, new byte[0]);
        }
        if (opcode.isControl() && fin) {
            return new WebSocketFrame(false, Opcode.UNKNOWN, new byte[0]);
        }
        if (payloadLen == 126) {
            byte[] extended = in.readNBytes(2);
            payloadLen = ByteBuffer.wrap(extended).getShort() & 0xFFFF;
        } else if (payloadLen == 127) {
            byte[] extended = in.readNBytes(8);
            long len = ByteBuffer.wrap(extended).getLong();
            if (len > Integer.MAX_VALUE) {
                return new WebSocketFrame(false, Opcode.OPCODE_CLOSE, new byte[0]);
            }
            payloadLen = (int) len;
        }
        byte[] maskingKey = in.readNBytes(4);
        byte[] payload = in.readNBytes(payloadLen);
        for (int i = 0; i < payload.length; i++) {
            payload[i] ^= maskingKey[i % 4];
        }
        return new WebSocketFrame(fin, opcode, payload);
    }

    static void send(byte[] data, boolean fin, Socket socket, Opcode opcode) throws IOException {
        if (Opcode.OPCODE_TEXT == opcode) {
            data = new String(data, StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8);
        }
        int len = data.length;
        ByteArrayOutputStream frame = new ByteArrayOutputStream();
        int firstByte = (fin ? 0x80 : 0x00) | (opcode.code & 0x0F);
        frame.write(firstByte);
        if (len <= 125) {
            frame.write(len);
        } else if (len <= 65535) {
            frame.write(126);
            frame.write((len >>> 8) & 0xFF);
            frame.write(len & 0xFF);
        } else {
            frame.write(127);
            for (int i = 7; i >= 0; i--) {
                frame.write((len >>> (8 * i)) & 0xFF);
            }
        }
        frame.write(data);

        OutputStream out = socket.getOutputStream();
        out.write(frame.toByteArray());
        out.flush();
    }
}