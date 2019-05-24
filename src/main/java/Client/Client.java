package Client;

public class Client {

    public static void main(String[] args) throws Exception {
        ServerInfo server = UDPSearcher.search();
        System.out.println(server);
        TCPClient client = new TCPClient();
        client.link(server);
    }
}
