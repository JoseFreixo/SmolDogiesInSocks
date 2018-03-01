import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class Client {
    private static int timeout = 10 * 1000;

    public static void main(String[] args) throws IOException{
        if (args.length < 4) {
            System.out.println("Usage: java Client <mcast_addr> <mcast_port> <oper> <opnd> * ");
            return;
        }

        // Criar a mensagem para o servidor
        String message = "Manel";

        if (args[2].equals("register") || args[2].equals("lookup")){
            message = String.join(":", Arrays.copyOfRange(args,2,args.length));
        } else if (args[2].equals("manel")) {
            System.out.println("I see you're a man of culture as well!");
        } else {
            System.out.println("You did it!");
        }

        // Criar o socket multicast para receber o port do server
        MulticastSocket multicastSocket = new MulticastSocket(Integer.parseInt(args[1]));
        multicastSocket.setSoTimeout(timeout);

        // Criar e receber o packet do port do server
        byte[] packbuf = new byte[1024];
        DatagramPacket multicastPacket = new DatagramPacket(packbuf, packbuf.length);
        multicastSocket.joinGroup(InetAddress.getByName(args[0]));
        multicastSocket.setSoTimeout(timeout);
        multicastSocket.receive(multicastPacket);
        String info = new String(multicastPacket.getData()).trim();
        String[] srvcInfo = info.split(":");

        System.out.println(srvcInfo[0] + " - " + srvcInfo[1]);

        // Criar o socket para enviar a mensagem e enviar
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(timeout);
        byte[] sbuf = message.getBytes();
        InetAddress address = InetAddress.getByName(srvcInfo[0]);
        DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, Integer.parseInt(srvcInfo[1]));

        // Receber a resposta do server
        byte[] rbuf = new byte[sbuf.length];
        DatagramPacket rpacket = new DatagramPacket(rbuf, rbuf.length);

        boolean cond = true;
        while(cond) {
            socket.send(packet);
            System.out.println("Sent the info to !");

            try {
                socket.receive(rpacket);
                cond = false;
            } catch (SocketTimeoutException e) {
                System.out.println("Server timed out|");
            }
        }

//        for (int i = 0; i < args.length; i++) {
        System.out.println(new String(rpacket.getData()));
//        }
    }
}
