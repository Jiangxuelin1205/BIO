package Utils;

import CommunicateException.InvalidPacketException;
import Server.ClientInfo;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Request {

    private static final byte[] HEADER = {8, 8, 8, 8, 8, 8, 8, 8};
    private static final int MIN_LENGTH = HEADER.length + 2 + 4;

    public static byte[] wrap(int listenPort) {
        ByteBuffer sendBuffer = ByteBuffer.allocate(HEADER.length + Short.BYTES + Integer.BYTES);
        sendBuffer.put(HEADER);
        sendBuffer.putShort((short) 2);
        sendBuffer.putInt(listenPort);
        return Arrays.copyOf(sendBuffer.array(), sendBuffer.position() + 1);
    }


    public static ClientInfo unwrap(DatagramPacket receivedPacket) throws InvalidPacketException {
        if (receivedPacket.getData().length < MIN_LENGTH) {
            throw new InvalidPacketException("Received packet is invalid");
        }
        byte[] data = receivedPacket.getData();
        ByteBuffer receivedBuffer = ByteBuffer.wrap(data, 0, data.length);
        byte[] header = new byte[HEADER.length];
        receivedBuffer.get(header, 0, HEADER.length);
        if (!Arrays.equals(header, HEADER)) {
            throw new InvalidPacketException("Received packet header is invalid");
        }
        return new ClientInfo(receivedPacket.getAddress().getHostAddress(), receivedPacket.getPort());

    }
}
