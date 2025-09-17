package org.ps;

record AssembledMessage(Type type, byte[] data) {

}

record WebSocketFrame(boolean fin, Opcode opCode, byte[] payload) {
}

enum Opcode {
    OPCODE_CONT(0x0),
    OPCODE_TEXT(0x1),
    OPCODE_BINARY(0x2),
    OPCODE_CLOSE(0x8),
    OPCODE_PING(0x9),
    OPCODE_PONG(0xA),
    UNMASKED(-2),
    UNKNOWN(-1);

    final int code;

    Opcode(int code) {
        this.code = code;
    }

    static Opcode fromCode(int code) {
        for (Opcode opCode : Opcode.values()) {
            if (opCode.code == code) {
                return opCode;
            }
        }
        return UNKNOWN;
    }

    boolean isControl() {
        return this == OPCODE_CLOSE || this == OPCODE_PING || this == OPCODE_PONG;
    }
}

enum Type {
    TEXT,
    BINARY,
    PART,
    ERROR;

    Opcode opcode() {
        return switch (this) {
            case TEXT -> Opcode.OPCODE_TEXT;
            case BINARY -> Opcode.OPCODE_BINARY;
            default -> Opcode.UNKNOWN;
        };
    }
}
