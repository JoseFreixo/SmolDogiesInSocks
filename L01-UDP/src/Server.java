import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Vector;

public class Server {

    public static void main(String args[]) throws IOException{
        Vector<String> matriculas;
        Vector<String> donos;

        if (args.length != 1) {
            System.out.println("Usage: java Server <port_number>");
            return;
        }
        DatagramSocket socket = new DatagramSocket(Integer.parseInt(args[0]));
        byte[] sbuf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length);

        while (true) {

            socket.receive(packet);
            String info = new String(packet.getData());
            System.out.println(info);

            if (false)
                break;
        }

        System.out.println(args[0]);
    }
}

