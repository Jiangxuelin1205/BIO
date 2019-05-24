package Server;

import CommunicateException.InvalidPacketException;
import Utils.Request;
import Utils.Response;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class UDPProvider {

    private final static int LISTEN_PORT = 10204;
    private final static int TCP_PORT = 10001;

    static void listen() {
        Provider provider = new Provider();
        provider.run();
    }

    static class Provider implements Runnable {
        @Override
        public void run() {
            System.out.println("UDP 准备就绪");
            try (DatagramSocket provider = new DatagramSocket(LISTEN_PORT)) {
                //noinspection InfiniteLoopStatement
                while (true) {
                    DatagramPacket receivePacket = receive(provider);
                    ClientInfo clientInfo = Request.unwrap(receivePacket);
                    response(provider, clientInfo);
                }
            } catch (InvalidPacketException | IOException e) {
                e.printStackTrace();
            }
        }

        private void response(DatagramSocket provider, ClientInfo clientInfo) throws IOException {
            byte[] bytes = Response.wrap(TCP_PORT);
            DatagramPacket packet = new DatagramPacket(bytes, 0, bytes.length, InetAddress.getByName(clientInfo.ip), clientInfo.port);
            provider.send(packet);
        }

        private DatagramPacket receive(DatagramSocket provider) throws IOException {
            DatagramPacket packet = new DatagramPacket(new byte[Response.MAX_LENGTH], 0, Response.MAX_LENGTH);
            provider.receive(packet);
            return packet;
        }
    }


}
