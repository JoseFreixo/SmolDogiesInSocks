import java.io.IOException;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class Server {
    private static int timeout = 1000;

    public static void main(String args[]) throws IOException{

        if (args.length != 3) {
            System.out.println("Usage: java Server <srvc_port> <mcast_addr> <mcast_port>");
            return;
        }
        MulticastSocket multicastSocket = new MulticastSocket(Integer.parseInt(args[2]));
        multicastSocket.setSoTimeout(timeout);
        multicastSocket.setTimeToLive(1);

        DatagramSocket socket = new DatagramSocket(Integer.parseInt(args[0]));
        socket.setSoTimeout(timeout);

//      int port = Integer.parseInt(args[0]);
        InetAddress address = InetAddress.getByName(args[1]);
        String local = InetAddress.getLocalHost().getHostName();

        String message = local + ":" + args[0];

        byte[] sbuf = message.getBytes();
        DatagramPacket announce = new DatagramPacket(sbuf, sbuf.length, address, Integer.parseInt(args[2]));
        byte[] rbuf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);

        while (true) {

            try {
                System.out.println("Waiting for a Client");
                socket.receive(packet);
                System.out.println("Received stuff! Manel! " + new String(packet.getData()).trim());
                packet.setData("WE DID IT REDDIT".getBytes());
                socket.send(packet);

            } catch (SocketTimeoutException e) {
                multicastSocket.send(announce);
            }

            if (false)
                break;
        }

        System.out.println(args[0]);
    }
}

