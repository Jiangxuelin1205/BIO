package Server;

public interface CallBack {

    void onNewMessageArrived(String message, ClientHandler handler);

    void closeClient(ClientHandler clientHandler);
}
