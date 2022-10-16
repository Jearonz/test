import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;

public class Client {
    public static String ipAddr = "localhost";
    public static int port;
    public static String type;
    public static final int PORT = 8080;
    public static LinkedList<ServerSomething> serverList = new LinkedList<>(); // список всех
    public static Story story; // история переписки

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        System.out.println("Server or client?");
        type = in.nextLine();
        if (type.equals("Server")) {
            new Server();
        }
        else if (type.equals("Client")) {
            System.out.print("Input IP: ");
            ipAddr = in.nextLine();
            System.out.print("Input Port: ");
            port = in.nextInt();
            new ClientSomething(ipAddr,port);
        }

    }
}

class Server extends Thread {
    public static final int PORT = 8080;
    public static LinkedList<ServerSomething> serverList = new LinkedList<>(); // список всех нитей
    public static Story story; // история переписки
    public String str;

    Server() throws IOException {
        start();
    }

    @Override
    public void run() {
        ServerSocket server = null;
        try {
            server = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        story = new Story();
        System.out.println("Server Started");
        Scanner in = new Scanner(System.in);
        try{
            while (true){
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
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class ClientSomething {
    private Socket socket;
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток чтения в сокет
    private BufferedReader inputUser; // поток чтения с консоли
    private String addr; // ip адрес клиента
    private int port; // порт соединения
    private String nickname; // имя клиента
    private Date time;
    private String dtime;
    private SimpleDateFormat dt1;

    public ClientSomething(String addr, int port){
        this.addr = addr;
        this.port = port;
        try{
            this.socket = new Socket(addr, port);
        } catch (IOException e){
            System.out.println("Socket failed");
        }
        try {
            inputUser = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.pressNickname(); // перед началом необходимо спросить имя
            new ReadMsg().start(); // нить читающая сообщения из сокета в бесконечном цикле
            new WriteMsg().start(); // нить пишущая сообщения в сокет приходящие с консоли в бесконечном цикле
        } catch (IOException e) {
            // Сокет должен быть закрыт при любой
            // ошибке, кроме ошибки конструктора сокета:
            ClientSomething.this.downService();
        }
        // В противном случае сокет будет закрыт
        // в методе run() нити.
    }
    public void pressNickname(){
        System.out.println("Press your nick: ");
        try{
            nickname = inputUser.readLine();
            out.write("Hello " + nickname + "\n");
            out.flush(); //Выталкиваем содержимое буфера
        }catch (IOException ignored){}
    }

    private void downService(){
        try{
            if (!socket.isClosed()){
                socket.close();
                in.close();
                out.close();
            }
        }catch (IOException ignored){}
    }

    // нить чтения сообщений с сервера
    private class ReadMsg extends Thread{
        @Override
        public void run(){
            String str;
            try {
                while (true) {
                    str = in.readLine(); // ждем сообщения с сервера
                    if (str.equals("stop")) {
                        ClientSomething.this.downService();
                        break;
                    }
                    System.out.println(str); // пишем сообщение с сервера на консоль
                }
            } catch (IOException e){
                ClientSomething.this.downService();
            }
        }
    }

    // нить отправляющая сообщения приходящие с консоли на сервер
    public class WriteMsg extends Thread{

        @Override
        public void run(){
            while (true){
                String userWord;

                try{
                    time = new Date(); //текущая дата
                    dt1 = new SimpleDateFormat("HH:mm:ss"); // берем только время до секунд
                    dtime = dt1.format(time); //время
                    userWord = inputUser.readLine();// сообщения с консоли
                    if (userWord.equals("stop")) {
                        out.write("stop" + "\n");
                        ClientSomething.this.downService();
                        break;
                    } else {
                        out.write("(" + dtime + ") " + nickname + ": " + userWord + "\n"); // отправляем на сервер
                    }
                    out.flush();
                }catch (IOException e){
                    ClientSomething.this.downService();
                }
            }
        }
    }
}


class ServerSomething extends Thread {
    private Socket socket; // сокет, через который сервер общается с клиентом,
    // кроме него - клиент и сервер никак не связаны
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток записи в сокет

    public ServerSomething(Socket socket) throws IOException{
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        start();// вызываем run()
    }

    @Override
    public void run(){
        String word;
        try {
            // первое сообщение отправленное сюда - это никнейм
            word = in.readLine();
            try {
                out.write(word + "\n");
                out.flush();// flush() нужен для выталкивания оставшихся данных
                // если такие есть, и очистки потока для дальнейших нужд
            }catch (IOException ignored){}
            try{
                while (true){
                    word = in.readLine();
                    if (word.equals("stop")){
                        this.downService();
                        break;
                    }
                    System.out.println("Echoing: " + word);
                    Server.story.addStoryE1(word);
                    for (ServerSomething serv: Server.serverList) {
                        serv.send(word); // отослать принятое сообщение с привязанного клиента всем остальным включая его
                    }
                }
            }catch (NullPointerException ignored) {

            }
        }catch (IOException e){
            this.downService();
        }
    }

    private void send(String msg){
        try{
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {}
    }

    private void downService(){
        try{
            if (!socket.isClosed()){
                socket.close();
                in.close();
                out.close();
                for (ServerSomething serv: Server.serverList) {
                    if (serv.equals(this)) serv.interrupt();
                    Server.serverList.remove(this);
                }
            }
        }catch (IOException ignored){}
    }
}

class Story {
    private LinkedList<String> story = new LinkedList<>();

    public void addStoryE1(String e1){
        // если сообщений больше 10, удаляем первое и добавляем новое
        // иначе просто добавить
        if (story.size() >= 10) {
            story.removeFirst();
            story.add(e1);
        }
        else {
            story.add(e1);
        }
    }

    public void printStory(BufferedWriter writer){
        if (story.size() > 0) {
            try{
                writer.write("History messages" + "\n");
                for (String vr: story) {
                    writer.write(vr + "\n");
                }
                writer.write("/...." + "\n");
                writer.flush();
            } catch (IOException ignored){}
        }
    }
}

