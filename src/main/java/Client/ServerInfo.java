package Client;

class ServerInfo {
    final String ip;
    final int port;

    ServerInfo(String ip, int port) {
        this.port = port;
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
