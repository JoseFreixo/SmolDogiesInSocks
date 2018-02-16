import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class Client {
    private static int timeout = 1000;

    public static void main(String[] args) throws IOException{
        if (args.length < 4) {
            System.out.println("Usage: java Client <host_name> <port_number> <oper> <opnd>*");
            return;
        }

        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(timeout);
        String message = "Manel";

        if (args[2].equals("register") || args[2].equals("lookup")){
            message = String.join(":", Arrays.copyOfRange(args,2,args.length));
        } else if (args[2].equals("manel")) {
            System.out.println("I see you're a man of culture as well!");
        } else {
            System.out.println("You did it!");
        }

        System.out.println(message);

        byte[] sbuf = message.getBytes();
        InetAddress address = InetAddress.getByName(args[0]);
        DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, Integer.parseInt(args[1]));

        socket.send(packet);

        byte[] rbuf = new byte[sbuf.length];
        packet = new DatagramPacket(rbuf, rbuf.length);

        try {
            socket.receive(packet);
        } catch (SocketTimeoutException e) {
            System.out.println("Server timed out|");
            return;
        }

//        for (int i = 0; i < args.length; i++) {
        System.out.println(packet.getData());
//        }
    }
}
