package Server;

import java.io.IOException;

public class Server {

    public static void main(String[] args) throws IOException {

        TCPServer server = new TCPServer(10001);
        server.start();

        UDPProvider.listen();
    }
}
