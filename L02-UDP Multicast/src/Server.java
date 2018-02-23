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

        byte[] sbuf = args[0].getBytes();
        int port = Integer.parseInt(args[0]);
        InetAddress address = InetAddress.getByName(args[1]);
        DatagramPacket announce = new DatagramPacket(sbuf, sbuf.length, address, Integer.parseInt(args[2]));
//      String message = ""
        byte[] rbuf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);

        while (true) {

            try {
                socket.receive(packet);
                System.out.println("Received stuff! Manel!" + packet.getData().toString());
            } catch (SocketTimeoutException e) {
                multicastSocket.send(announce);
                System.out.println("multicast: <" + args[1] + "> <" + args[2] + ">: <" + announce.getData().toString());
            }

            if (false)
                break;
        }

        System.out.println(args[0]);
    }
}

