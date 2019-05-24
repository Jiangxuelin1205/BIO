package Utils;

import CommunicateException.InvalidPacketException;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Response {

    private static final byte[] HEADER = {8, 8, 8, 8, 8, 8, 8, 8};
    private static final int MIN_LENGTH = HEADER.length + 2 + 4;
    public static final int MAX_LENGTH = 64;

    /**
     * @return 服务端的TCP端口
     */
    public static int unwrap(byte[] bytes) throws InvalidPacketException {
        if (bytes.length < MIN_LENGTH) {
            throw new InvalidPacketException("Invalid packet, too short");
        }

        ByteBuffer receiveBuffer = ByteBuffer.wrap(bytes, 0, bytes.length);

        byte[] header = new byte[HEADER.length];
        receiveBuffer.get(header, 0, HEADER.length);
        if (!Arrays.equals(header, HEADER)) {
            throw new InvalidPacketException("Invalid packet.");
        }

        receiveBuffer.getShort();

        @SuppressWarnings("UnnecessaryLocalVariable")
        int port = receiveBuffer.getInt();

        return port;
    }

    public static byte[] wrap(int port){
        ByteBuffer sendBuffer = ByteBuffer.allocate(64);
        sendBuffer.put(HEADER);
        sendBuffer.putShort((short) 1);
        sendBuffer.putInt(port);
        return Arrays.copyOf(sendBuffer.array(),sendBuffer.position()+1);
    }
}
