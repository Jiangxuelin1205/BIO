package Server;

import Utils.Close;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class ClientHandler {

    private Socket client;
    private ReadHandler readHandler;
    private WriteHandler writeHandler;
    private CallBack callBack;

    ClientHandler(Socket client, CallBack callBack) throws IOException {
        this.client = client;
        readHandler = new ReadHandler(client.getInputStream());//仅仅做初始化，还没有开启线程
        writeHandler = new WriteHandler(client.getOutputStream());//仅仅初始化，还没有开启线程
        this.callBack = callBack;
    }

    void write(String message) {
        writeHandler.send(message);
    }

    void read() {
        Thread readThread = new Thread(readHandler);
        readThread.start();
    }

    void exit() {

        readHandler.exit();
        writeHandler.exit();
        Close.close(client);
        callBack.closeClient(this);
        System.out.println("客户端已经退出");
    }

    private class ReadHandler implements Runnable {

        private InputStream inputStream;

        ReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = client.getInputStream();
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

                String message;
                while ((message=socketInput.readLine())!=null) {
                    if (message.equalsIgnoreCase("bye")) {//如果从客户端获取的字符串是bye,则关闭当前的输入流和输出流
                        break;
                    }
                    System.out.println(message);
                    callBack.onNewMessageArrived(message, ClientHandler.this);
                }
                ClientHandler.this.exit();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Close.close(inputStream);
            }
        }

        void exit() {
            Close.close(inputStream);
        }
    }

    private class WriteHandler {//不需要负责消息的来源，直接将消息进行转发

        OutputStream outputStream;
        private final ExecutorService executorService;

        WriteHandler(OutputStream outputStream) {
            this.outputStream = outputStream;
            executorService = Executors.newSingleThreadExecutor();
        }

        void send(String message) {
            executorService.execute(new Writer(message));
        }

        void exit() {
            executorService.shutdown();
            Close.close(outputStream);
        }

        class Writer implements Runnable {

            String message;

            Writer(String message) {
                this.message = message;
            }

            @Override
            public void run() {
                try {
                    PrintStream output = new PrintStream(client.getOutputStream());
                    output.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}