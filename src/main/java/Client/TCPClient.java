package Client;

import Utils.Close;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

class TCPClient {

    private Socket socket;

    void link(ServerInfo serverInfo) throws IOException, InterruptedException {
        socket = new Socket(serverInfo.ip, serverInfo.port);
        CountDownLatch c = new CountDownLatch(2);

        System.out.println("客户端已经发起socket连接");
        System.out.println("客户端信息：" + socket.getLocalAddress() + ":" + socket.getLocalPort());//Returns the local port number to which this socket is bound.
        System.out.println("服务器端信息：" + socket.getInetAddress() + ":" + socket.getPort());//Returns the remote port number to which this socket is connected.

        try {
            WriteHandler writeHandler = new WriteHandler(socket, c);
            Thread writeThread = new Thread(writeHandler);
            writeThread.start();

            ReadHandler readHandler = new ReadHandler(socket, c);
            Thread readThread = new Thread(readHandler);
            readThread.start();

        } catch (Exception ignored) {

        }

        c.await();
        //socket.close();
        Close.close(socket);
        System.out.println("客户端已经退出");
    }

    private class ReadHandler implements Runnable {

        Socket server;
        CountDownLatch c;

        ReadHandler(Socket server, CountDownLatch c) {
            this.server = server;
            this.c = c;
        }

        @Override
        public void run() {
            try {
                BufferedReader serverInput = new BufferedReader(new InputStreamReader(server.getInputStream()));//从服务器返回
                String echo;
                while (!socket.isClosed() && (echo = serverInput.readLine()) != null) {
                    System.out.println(echo);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class WriteHandler implements Runnable {

        Socket server;
        CountDownLatch c;

        WriteHandler(Socket server, CountDownLatch c) {
            this.server = server;
            this.c = c;
        }

        @Override
        public void run() {

            try {
                BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
                PrintStream output = new PrintStream(server.getOutputStream());

                while (true) {
                    String message = consoleInput.readLine();
                    output.println(message);
                    if (message.equalsIgnoreCase("bye")) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                server.shutdownOutput();
                server.shutdownInput();
            } catch (IOException e) {
                e.printStackTrace();
            }
            c.countDown();
            c.countDown();

        }
    }

}
