package Server;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ClientHandler implements Runnable {

    private Socket client;
    private ReadHandler readHandler;
    private WriteHandler writeHandler;
    private CountDownLatch c;
    private CallBack callBack;

    ClientHandler(Socket client,CallBack callBack) {
        this.client = client;
        c = new CountDownLatch(2);
        readHandler = new ReadHandler(client, c);
        writeHandler = new WriteHandler(client, c);
        this.callBack=callBack;
    }

    @Override
    public void run() {
        System.out.println("新客户端的连接：" + client.getInetAddress() + " P:" + client.getPort());

        try {
            Thread read = new Thread(readHandler);
            read.start();

        } catch (Exception e) {
            System.out.println("连接异常断开");
        }

        try {
            c.await();
            callBack.closeClient(this);
            client.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("客户端已退出：" + client.getInetAddress() +
                " P:" + client.getPort());
    }

    void exit() throws IOException {
        client.close();
    }

    void send(String message){
        writeHandler.send(message);
    }

    private class ReadHandler implements Runnable {

        private Socket client;
        private CountDownLatch c;

        ReadHandler(Socket client, CountDownLatch c) {
            this.client = client;
            this.c = c;
        }

        @Override
        public void run() {
            BufferedReader buffer = null;
            try {
                buffer = new BufferedReader(new InputStreamReader(client.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    assert buffer != null;
                    String message = buffer.readLine();
                    System.out.println(message);
                    if (message.equalsIgnoreCase("bye")) {//如果收到的是“bye”，则服务器端关闭该客户端的输入流和输出流
                        break;
                    }
                    callBack.onNewMessageArrived(message,ClientHandler.this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                client.shutdownInput();
                client.shutdownOutput();
            } catch (IOException e) {
                e.printStackTrace();
            }
            c.countDown();
            c.countDown();
        }
    }

    private class WriteHandler {//不需要负责消息的来源，直接将消息进行转发

        Socket client;
        CountDownLatch c;
        private final ExecutorService executorService;

        WriteHandler(Socket client, CountDownLatch c) {
            this.client = client;
            this.c = c;
            executorService = Executors.newSingleThreadExecutor();
        }

        void send(String message) {
            executorService.execute(new Writer(message));
        }

        class Writer implements Runnable {

            String message;

            Writer(String message) {
                this.message = message;
            }

            @Override
            public void run() {
                try {
                    PrintStream output=new PrintStream(client.getOutputStream());
                    output.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}