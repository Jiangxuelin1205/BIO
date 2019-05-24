package Server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class TCPServer implements CallBack {

    private final List<ClientHandler> clientList;
    private Listener listener;
    private final ExecutorService e;

    TCPServer(int port) throws IOException {
        clientList = new ArrayList<>();
        this.listener = new Listener(port);
        e = Executors.newSingleThreadExecutor();
    }

    void start() {
        Thread t = new Thread(listener);
        t.start();
    }

    public void stop() {
        if (listener != null) {
            listener.exit();
        }
        for (ClientHandler clientHandler : clientList) {
            clientHandler.exit();
        }
        clientList.clear();
        e.shutdown();
    }

    public void broadcast(String message) {
        for (ClientHandler client : clientList) {
            client.write(message);
        }
    }


    @Override
    public void onNewMessageArrived(String message, ClientHandler handler) {
        e.execute(() -> {
            synchronized (TCPServer.this) {
                for (ClientHandler clientHandler : clientList) {
                    if (clientHandler != handler) {
                        clientHandler.write(message);
                    }
                }
            }
        });
    }

    @Override
    public void closeClient(ClientHandler client) {
        synchronized (this) {
            clientList.remove(client);
        }
    }

    private class Listener implements Runnable {

        private ServerSocket server;

        Listener(int port) throws IOException {//服务器端开一个线程，进行监听
            server = new ServerSocket(port);
        }

        @Override
        public void run() {
            System.out.println("服务器准备就绪");
            try {
                //noinspection InfiniteLoopStatement
                do {
                    Socket client = server.accept();
                    ClientHandler clientHandler = new ClientHandler(client, TCPServer.this);
                    synchronized (TCPServer.this) {
                        clientList.add(clientHandler);
                    }
                    clientHandler.read();

                } while (true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void exit() {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
