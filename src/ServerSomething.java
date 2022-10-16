import java.io.*;
import java.net.Socket;

public class ServerSomething extends Thread {
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