import java.util.Scanner;

public class Client {
    public static String ipAddr;
    public static int port;

    public static void main(String[] args){
        Scanner in = new Scanner(System.in);

        System.out.print("Input IP: ");
        ipAddr = in.nextLine();
        System.out.print("Input Port: ");
        port = in.nextInt();

        new ClientSomething(ipAddr,port);
    }
}