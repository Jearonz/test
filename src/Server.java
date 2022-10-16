import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
    public static final int PORT = 8080;
    public static LinkedList<ServerSomething> serverList = new LinkedList<>(); // список всех
    public static Story story; // история переписки

    public static void main(String[] args) throws IOException{
        ServerSocket server = new ServerSocket(PORT);
        story = new Story();
        System.out.println("Server started!");
        try {
            while (true) {
                // Блокируется до возникновения нового соединения:
                Socket socket = server.accept();
                try {
                    serverList.add(new ServerSomething(socket)); // добавить новое соединенние в список
                } catch (IOException e){
                    // Если завершится неудачей, закрывается сокет,
                    // в противном случае, нить закроет его при завершении работы:
                    socket.close();
                }
            }
        } finally {
            System.out.println("Server closed!");
            server.close();
        }
    }
}