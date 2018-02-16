import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {

    public static void main(String args[]) throws IOException{

        if (args.length != 1) {
            System.out.println("Usage: java Server <port_number>");
            return;
        }
        DatagramSocket socket = new DatagramSocket(Integer.parseInt(args[0]));
//        DatagramPacket packet = new DatagramPacket();

        while (true) {

//            socket.receive()

            if (false)
                break;
        }

        System.out.println(args[0]);
    }
}

