import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SocketRunnable implements Runnable {
    private static int peerId;
    private static InetAddress ip;
    private static int port;
    private static MulticastSocket socket;

    public SocketRunnable(InetAddress ip, int port, int peerId) throws IOException {
        this.ip = ip;
        this.port = port;
        socket = new MulticastSocket(this.port);
        this.peerId = peerId;
    }

    @Override
    public void run() {
        System.out.println("Manel1");
        byte[] rbuf = new byte[70000];
        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
