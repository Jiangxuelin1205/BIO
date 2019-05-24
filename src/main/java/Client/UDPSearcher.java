package Client;

import CommunicateException.InvalidPacketException;
import Utils.Request;
import Utils.Response;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class UDPSearcher {

    private final static int LISTEN_PORT = 10203;
    private final static int SERVER_PORT = 10204;

    static ServerInfo search() throws Exception {
        try (DatagramSocket searcher = new DatagramSocket()) {
            broadcast(searcher, Request.wrap(LISTEN_PORT));
            //noinspection LoopStatementThatDoesntLoop
            while (true) {
                DatagramPacket response = receive(searcher);
                ServerInfo serverInfo;
                try {
                    serverInfo = getServerInfo(response);
                } catch (InvalidPacketException e) {
                    continue;
                }
                return serverInfo;
            }
        }
    }

    private static ServerInfo getServerInfo(DatagramPacket response) throws InvalidPacketException {
        String ip = response.getAddress().getHostAddress();
        int port;
        port = Response.unwrap(response.getData());
        return new ServerInfo(ip, port);
    }

    private static void broadcast(DatagramSocket searcher, byte[] array) throws IOException {
        DatagramPacket packet = new DatagramPacket(array, 0, array.length);
        packet.setAddress(InetAddress.getByName("255.255.255.255"));
        packet.setPort(SERVER_PORT);
        searcher.send(packet);
    }

    private static DatagramPacket receive(DatagramSocket searcher) throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[Response.MAX_LENGTH], Response.MAX_LENGTH);
        searcher.receive(packet);

        return packet;
    }
}
