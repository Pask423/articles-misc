package org.ps;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

final class FragmentAssembler {
    private ByteArrayOutputStream buffer;
    private Opcode opcode = Opcode.UNKNOWN;

    AssembledMessage accept(WebSocketFrame f) {
        if (f.opCode() == Opcode.OPCODE_CONT) {
            if (buffer == null) {
                return new AssembledMessage(Type.ERROR, new byte[0]);
            }
            append(f.payload());
            if (f.fin()) {
                byte[] fullMessage = buffer.toByteArray();
                Type type = opcode == Opcode.OPCODE_TEXT ? Type.TEXT : Type.BINARY;
                buffer = null;
                opcode = Opcode.UNKNOWN;
                return new AssembledMessage(type, fullMessage);
            }
            return new AssembledMessage(Type.PART, new byte[0]);
        }

        if (f.opCode() == Opcode.OPCODE_TEXT || f.opCode() == Opcode.OPCODE_BINARY) {
            if (buffer != null) {
                return new AssembledMessage(Type.ERROR, new byte[0]);
            }
            if (f.fin()) {
                Type type = f.opCode() == Opcode.OPCODE_TEXT ? Type.TEXT : Type.BINARY;
                return new AssembledMessage(type, f.payload());
            } else {
                buffer = new ByteArrayOutputStream(Math.max(1024, f.payload().length));
                opcode = f.opCode();
                append(f.payload());
                return new AssembledMessage(Type.PART, new byte[0]);
            }
        }
        return new AssembledMessage(Type.ERROR, new byte[0]);
    }

    private void append(byte[] chunk) {
        try {
            buffer.write(chunk);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}